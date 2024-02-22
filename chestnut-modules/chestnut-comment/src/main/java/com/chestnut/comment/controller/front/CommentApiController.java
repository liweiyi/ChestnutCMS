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
package com.chestnut.comment.controller.front;

import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.dto.SubmitCommentDTO;
import com.chestnut.comment.domain.vo.CommentVO;
import com.chestnut.comment.service.ICommentApiService;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.annotation.IgnoreDemoMode;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentApiController extends BaseRestController {

	private final ICommentApiService commentApiService;

	/**
	 * 评论列表，按时间（ID）倒序
	 * 
	 * @param type
	 * @param dataId
	 * @return
	 */
	@GetMapping("/{type}/{dataId}")
	public R<?> getCommentList(@PathVariable("type") String type, @PathVariable("dataId") Long dataId,
			@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
			@RequestParam(value = "offset", defaultValue = "0") Long offset) {
		List<CommentVO> list = this.commentApiService.getCommentList(type, dataId, limit, offset);
		return R.ok(list);
	}

	/**
	 * 获取评论回复列表，按时间（ID）倒序
	 * 
	 * @param commentId
	 * @param limit
	 * @param offset
	 * @return
	 */
	@GetMapping("/reply/{commentId}")
	public R<?> getCommentReplyList(@PathVariable @Min(1) Long commentId,
			@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
			@RequestParam(value = "offset", defaultValue = "0") Long offset) {
		List<CommentVO> list = this.commentApiService.getCommentReplyList(commentId, limit, offset);
		return R.ok(list);
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/submit")
	public R<?> submitComment(@RequestBody SubmitCommentDTO dto) {
		dto.setOperator(StpMemberUtil.getLoginUser());
		dto.setClientIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		dto.setUserAgent(ServletUtils.getUserAgent());
		Comment comment = this.commentApiService.submitComment(dto);
		return R.ok(comment);
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PutMapping("/like/{commentId}")
	public R<?> likeComment(@PathVariable @Min(1) Long commentId) {
		this.commentApiService.likeComment(commentId, StpMemberUtil.getLoginIdAsLong());
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@DeleteMapping("/{commentId}")
	public R<?> deleteMyComment(@PathVariable @Min(1) Long commentId) {
		this.commentApiService.deleteUserComment(StpMemberUtil.getLoginIdAsLong(), commentId);
		return R.ok();
	}
}
