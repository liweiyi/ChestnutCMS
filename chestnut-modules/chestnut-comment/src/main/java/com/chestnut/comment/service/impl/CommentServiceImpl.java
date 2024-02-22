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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.comment.CommentConsts;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.dto.AuditCommentDTO;
import com.chestnut.comment.fixed.dict.CommentAuditStatus;
import com.chestnut.comment.mapper.CommentMapper;
import com.chestnut.comment.service.ICommentService;
import com.chestnut.common.db.DBConstants;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditComment(AuditCommentDTO dto) {
		int auditStatus = YesOrNo.isYes(dto.getAuditFlag()) ? CommentAuditStatus.PASSED : CommentAuditStatus.NOT_PASSED;
		this.lambdaUpdate().set(Comment::getAuditStatus, auditStatus).in(Comment::getCommentId, dto.getCommentIds())
				.update();
	}

	/**
	 * 后台删除评论仅做删除标识，不影响上级评论回复数，前端依然可见评论/回复项，但是内容不可见
	 */
	@Override
	public void deleteComments(List<Long> commentIds) {
		this.lambdaUpdate().set(Comment::getDelFlag, CommentConsts.DELETE_FLAG).in(Comment::getCommentId, commentIds)
				.update();
	}
}