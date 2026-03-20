/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.comment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.CommentLike;
import com.chestnut.comment.domain.dto.AuditCommentDTO;
import com.chestnut.comment.permission.CommentPriv;
import com.chestnut.comment.service.ICommentLikeService;
import com.chestnut.comment.service.ICommentService;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;


import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@XComment("{API.DOC.COMMENT.MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController extends BaseRestController {

	private final ICommentService commentService;

	private final ICommentLikeService commentLikeService;

	@XComment("{API.DOC.COMMENT.GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping("/list")
	public R<TableData<Comment>> getCommentList(@RequestParam(required = false) @XComment("{API.DOC.COMMENT.SOURCE_TYPE}") String sourceType,
							   @RequestParam(required = false) @XComment("{API.DOC.COMMENT.SOURCE_ID}") String sourceId,
							   @RequestParam(required = false) @XComment("{API.DOC.COMMENT.UID}") Long uid,
							   @RequestParam(required = false) @XComment("{API.DOC.COMMENT.AUDIT_STATUS}") Integer auditStatus) {
		PageRequest pr = this.getPageRequest();

		Page<Comment> page = this.commentService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(sourceType), Comment::getSourceType, sourceType)
				.eq(StringUtils.isNotEmpty(sourceId), Comment::getSourceId, sourceId)
				.eq(Comment::getParentId, 0)
				.eq(IdUtils.validate(uid), Comment::getUid, uid)
				.eq(Objects.nonNull(auditStatus), Comment::getAuditStatus, auditStatus)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));

		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.COMMENT.GET_REPLY_LIST}")
	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping("/reply/{commentId}")
	public R<TableData<Comment>> getCommentReplyList(@PathVariable @LongId @XComment("{API.DOC.COMMENT.COMMENT_ID}") Long commentId) {
		PageRequest pr = this.getPageRequest();
		Page<Comment> page = this.commentService.lambdaQuery()
				.eq(IdUtils.validate(commentId), Comment::getParentId, commentId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.COMMENT.GET_LIKE_LIST}")
	@Priv(type = AdminUserType.TYPE, value = CommentPriv.View)
	@GetMapping("/like/{commentId}")
	public R<TableData<CommentLike>> getCommentLikeList(@PathVariable @LongId @XComment("{API.DOC.COMMENT.COMMENT_ID}") Long commentId,
														@RequestParam(required = false) @XComment("{API.DOC.COMMENT.UID}") Long uid) {
		PageRequest pr = this.getPageRequest();
		Page<CommentLike> page = this.commentLikeService.lambdaQuery()
				.eq(IdUtils.validate(commentId), CommentLike::getCommentId, commentId)
				.eq(IdUtils.validate(uid), CommentLike::getUid, uid).orderByDesc(CommentLike::getLogId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.COMMENT.AUDIT}")
	@Priv(type = AdminUserType.TYPE, value = CommentPriv.Audit)
	@PostMapping("/audit")
	public R<Void> auditComment(@RequestBody AuditCommentDTO dto) {
		this.commentService.auditComment(dto);
		return R.ok();
	}

	@XComment("{API.DOC.COMMENT.DELETE}")
	@Priv(type = AdminUserType.TYPE, value = CommentPriv.Delete)
	@PostMapping("/delete")
	public R<Void> deleteComment(@RequestBody @NotEmpty @XComment("{API.DOC.COMMENT.COMMENT_IDS}") List<Long> commentIds) {
		Assert.isTrue(IdUtils.validate(commentIds), CommonErrorCode.INVALID_REQUEST_ARG::exception);
		this.commentService.deleteComments(commentIds);
		return R.ok();
	}

	@XComment("{API.DOC.COMMENT.RECOVER}")
    @Priv(type = AdminUserType.TYPE, value = CommentPriv.Delete)
    @PostMapping("/recover")
    public R<Void> recoverComment(@RequestBody @NotEmpty @XComment("{API.DOC.COMMENT.COMMENT_IDS}") List<Long> commentIds) {
        Assert.isTrue(IdUtils.validate(commentIds), CommonErrorCode.INVALID_REQUEST_ARG::exception);
        this.commentService.recoverComment(commentIds);
        return R.ok();
    }
}
