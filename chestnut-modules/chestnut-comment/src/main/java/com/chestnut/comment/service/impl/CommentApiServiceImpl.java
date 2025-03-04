/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.comment.CommentConsts;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.CommentLike;
import com.chestnut.comment.domain.dto.SubmitCommentDTO;
import com.chestnut.comment.domain.vo.CommentVO;
import com.chestnut.comment.exception.CommentErrorCode;
import com.chestnut.comment.fixed.dict.CommentAuditStatus;
import com.chestnut.comment.listener.event.AfterCommentSubmitEvent;
import com.chestnut.comment.listener.event.BeforeCommentSubmitEvent;
import com.chestnut.comment.mapper.CommentLikeMapper;
import com.chestnut.comment.mapper.CommentMapper;
import com.chestnut.comment.member.CommentExpOperation;
import com.chestnut.comment.member.CommentMemberStatData;
import com.chestnut.comment.service.ICommentApiService;
import com.chestnut.comment.service.ICommentService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.service.IMemberExpConfigService;
import com.chestnut.member.service.IMemberStatDataService;
import com.chestnut.word.service.ISensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CommentApiServiceImpl implements ICommentApiService, ApplicationContextAware {

	private final CommentMapper commentMapper;

	private final CommentLikeMapper commentLikeMapper;

	private final ICommentService commentService;

	private final AsyncTaskManager asyncTaskManager;

	private final RedissonClient redissonClient;

	private final IMemberStatDataService memberStatDataService;

	private final IMemberExpConfigService memberExpConfigService;

	private final ISensitiveWordService sensitiveWordService;

	private ApplicationContext applicationContext;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void likeComment(Long commentId, long uid) {
		Comment comment = this.commentMapper.selectById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);

		this.incrCommentLikeCount(commentId, uid);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelLikeComment(Long commentId, long uid) {
		Comment comment = this.commentMapper.selectById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);

		this.decrCommentLikeCount(commentId, uid);
	}

	private void incrCommentLikeCount(Long commentId, long uid) {
		this.changeCommentLikeCount(commentId, uid, true);
	}

	private void decrCommentLikeCount(Long commentId, long uid) {
		this.changeCommentLikeCount(commentId, uid, false);
	}

	private void changeCommentLikeCount(Long commentId, long uid, boolean increase) {
		RLock lock = redissonClient.getLock("CommentLike-" + commentId);
		lock.lock();
		try {
			Comment comment = this.commentService.lambdaQuery()
					.select(Comment::getLikeCount, Comment::getCommentId, Comment::getUid)
					.eq(Comment::getCommentId, commentId).one();
			if (Objects.isNull(comment)) {
				return;
			}
			if (increase) {
				Long count = this.commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLike>()
						.eq(CommentLike::getCommentId, comment.getCommentId()).eq(CommentLike::getUid, uid));
				if (count > 0) {
					return;
				}
				this.commentService.lambdaUpdate().set(Comment::getLikeCount, comment.getLikeCount() + 1)
						.eq(Comment::getCommentId, comment.getCommentId()).update();

				CommentLike commentLike = new CommentLike();
				commentLike.setCommentId(comment.getCommentId());
				commentLike.setUid(uid);
				commentLike.setLikeTime(LocalDateTime.now());
				this.commentLikeMapper.insert(commentLike);
				// 触发会员经验值操作
				memberExpConfigService.triggerExpOperation(CommentExpOperation.ID, comment.getUid());
			} else {
				this.commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
						.eq(CommentLike::getCommentId, comment.getCommentId()).eq(CommentLike::getUid, uid));
				this.commentService.lambdaUpdate().set(Comment::getLikeCount, comment.getLikeCount() - 1)
						.eq(Comment::getCommentId, comment.getCommentId()).update();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Comment submitComment(SubmitCommentDTO dto) {
		Comment comment = new Comment();
		comment.setCommentId(IdUtils.getSnowflakeId());
		comment.setSourceType(dto.getSourceType());
		comment.setSourceId(dto.getSourceId());
		comment.setUid(dto.getOperator().getUserId());
		// 敏感词过滤
		String content = sensitiveWordService.replaceSensitiveWords(dto.getContent(), null);
		comment.setContent(content);
		comment.setCommentTime(LocalDateTime.now());
		comment.setAuditStatus(CommentAuditStatus.TO_AUDIT);
		comment.setLikeCount(0);
		comment.setParentId(0L);
		comment.setReplyCount(0);
		comment.setReplyUid(0L);
		comment.setIp(dto.getClientIp());
		comment.setUserAgent(dto.getUserAgent());
		comment.setLocation(IP2RegionUtils.ip2Region(dto.getClientIp()));
		comment.setClientType(ServletUtils.getDeviceType(dto.getUserAgent()));
		if (IdUtils.validate(dto.getCommentId())) {
			Comment parent = this.commentMapper.selectById(dto.getCommentId());
			if (!comment.getSourceType().equals(parent.getSourceType())
					|| !comment.getSourceId().equals(parent.getSourceId())
					|| parent.getParentId() != 0) {
				throw new RuntimeException("Reply comment not found: " + dto.getCommentId());
			}
			comment.setReplyUid(dto.getReplyUid());
			comment.setParentId(dto.getCommentId());
		}
		this.applicationContext.publishEvent(new BeforeCommentSubmitEvent(this, comment));
		this.commentService.save(comment);
		this.applicationContext.publishEvent(new AfterCommentSubmitEvent(this, comment));

		asyncTaskManager.execute(() -> {
			// 更新会员评论数量
			memberStatDataService.changeMemberStatData(comment.getUid(), CommentMemberStatData.TYPE, 1);
			// 如果是回复，修改父级评论回复数
			if (comment.getParentId() > 0) {
				incrCommentReplyCount(comment.getParentId());
			}
			// 触发会员经验值操作
			memberExpConfigService.triggerExpOperation(CommentExpOperation.ID, comment.getUid());
		});
		return comment;
	}

	@Override
	public List<CommentVO> getCommentList(String type, Long sourceId, Integer limit, Long offset) {
		Page<Comment> page = this.commentService.lambdaQuery().eq(Comment::getSourceType, type)
				.eq(Comment::getSourceId, sourceId)
				.eq(Comment::getParentId, 0)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED)
				.lt(IdUtils.validate(offset), Comment::getCommentId, offset)
				.orderByDesc(Comment::getCommentId)
				.page(new Page<>(1, limit, false));
		List<Comment> list = page.getRecords();
        return list.stream().map(comment -> {
			CommentVO vo = CommentVO.newInstance(comment);
			vo.setUser(this.memberStatDataService.getMemberCache(comment.getUid()));
			if (comment.getReplyCount() > 0) {
				List<CommentVO> replyList = this.loadCommentReplyList(comment.getCommentId(), 2, 0L)
						.stream().map(reply -> {
							CommentVO voReply = CommentVO.newInstance(reply);
							voReply.setUser(this.memberStatDataService.getMemberCache(reply.getUid()));
							if (reply.getReplyUid() > 0) {
								voReply.setReplyUser(this.memberStatDataService.getMemberCache(reply.getReplyUid()));
							}
							return voReply;
						}).toList();
				vo.setReplyList(replyList);
			}
			return vo;
		}).toList();
	}

	@Override
	public List<CommentVO> getCommentListByMember(String type, Long memberId, Integer limit, Long offset, boolean includeReply) {
		Page<Comment> page = this.commentService.lambdaQuery().eq(Comment::getSourceType, type)
				.eq(Comment::getUid, memberId)
				.eq(!includeReply, Comment::getParentId, 0)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED)
				.lt(IdUtils.validate(offset), Comment::getCommentId, offset)
				.orderByDesc(Comment::getCommentId)
				.page(new Page<>(1, limit, false));
		List<Comment> list = page.getRecords();
		MemberCache memberCache = this.memberStatDataService.getMemberCache(memberId);
        return list.stream().map(comment -> {
			CommentVO vo = CommentVO.newInstance(comment);
			vo.setUser(memberCache);
			if (comment.getReplyUid() > 0) {
				vo.setReplyUser(this.memberStatDataService.getMemberCache(comment.getReplyUid()));
			}
			return vo;
		}).toList();
	}

	private List<Comment> loadCommentReplyList(Long commentId, Integer limit, Long offset) {
		Page<Comment> page = this.commentService.lambdaQuery()
				.eq(Comment::getParentId, commentId)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED)
				.lt(IdUtils.validate(offset), Comment::getCommentId, offset)
				.orderByDesc(Comment::getCommentId).page(new Page<>(1, limit, false));
		return page.getRecords();
	}

	@Override
	public List<CommentVO> getCommentReplyList(Long commentId, Integer limit, Long offset) {
		List<Comment> list = loadCommentReplyList(commentId, limit, offset);
        return list.stream().map(reply -> {
			CommentVO vo = CommentVO.newInstance(reply);
			vo.setUser(this.memberStatDataService.getMemberCache(reply.getUid()));
			if (reply.getReplyUid() > 0) {
				vo.setReplyUser(this.memberStatDataService.getMemberCache(reply.getReplyUid()));
			}
			return vo;
		}).toList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteUserComment(Long userId, Long commentId) {
		Comment comment = this.commentService.getById(commentId);
		Assert.notNull(comment, CommentErrorCode.API_COMMENT_NOT_FOUND::exception);
		Assert.isTrue(Objects.equals(comment.getUid(), userId), CommentErrorCode.API_ACCESS_DENY::exception);
		// 直接评论且存在未删除回复的不直接删除，标记为删除状态
		if (comment.getParentId() == 0 && comment.getReplyCount() > 0) {
			this.commentService.lambdaUpdate()
					.set(Comment::getDelFlag, CommentConsts.DELETE_FLAG)
					.eq(Comment::getCommentId, commentId)
					.update();
		} else {
			this.commentService.remove(new LambdaQueryWrapper<Comment>()
					.eq(Comment::getCommentId, comment.getCommentId()).or().eq(Comment::getParentId, comment.getCommentId()));
		}
		if (IdUtils.validate(comment.getParentId())) {
			// 修改上级评论回复数
			this.decrCommentReplyCount(comment.getParentId());
		}
	}

	/**
	 * 修改评论回复数+1/-1
	 *
	 * @param commentId 评论ID
	 * @param increase  是否增加
	 */
	private void changeCommentReplyCount(Long commentId, boolean increase) {
		RLock lock = redissonClient.getLock("Comment-" + commentId);
		lock.lock();
		try {
			Comment comment = this.commentService.getById(commentId);
			if (Objects.isNull(comment)) {
				return;
			}
			if (increase) {
				this.commentService.lambdaUpdate().set(Comment::getReplyCount, comment.getReplyCount() + 1)
						.eq(Comment::getCommentId, comment.getCommentId()).update();
			} else {
				this.commentService.lambdaUpdate().set(Comment::getReplyCount, comment.getReplyCount() - 1)
						.eq(Comment::getCommentId, comment.getCommentId()).update();
			}
		} finally {
			lock.unlock();
		}
	}

	private void incrCommentReplyCount(Long commentId) {
		this.changeCommentReplyCount(commentId, true);
	}

	private void decrCommentReplyCount(Long commentId) {
		this.changeCommentReplyCount(commentId, false);
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
