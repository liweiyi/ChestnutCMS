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
package com.chestnut.comment.service;

import java.util.List;

import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.dto.SubmitCommentDTO;
import com.chestnut.comment.domain.vo.CommentVO;

public interface ICommentApiService {

	/**
	 * 提交评论
	 *
	 * @param dto
	 * @return
	 */
	Comment submitComment(SubmitCommentDTO dto);

	/**
	 * 获取评论列表
	 * 
	 * @param type
	 * @param dataId
	 * @param limit
	 * @param offset
	 * @return
	 */
	List<CommentVO> getCommentList(String type, Long dataId, Integer limit, Long offset);

	List<CommentVO> getCommentListByMember(String type, Long memberId, Integer limit, Long offset, boolean includeReply);

	/**
	 * 获取评论回复列表
	 *
	 * @param commentId
	 * @param limit
	 * @param offset
	 * @return
	 */
	List<CommentVO> getCommentReplyList(Long commentId, Integer limit, Long offset);

	/**
	 * 删除指定用户评论
	 * 
	 * @param userId
	 * @param commentId
	 */
	void deleteUserComment(Long userId, Long commentId);

	/**
	 * 评论点赞
	 * 
	 * @param commentId
	 * @param uid
	 */
	void likeComment(Long commentId, long uid);

	/**
	 * 取消评论点赞
	 * 
	 * @param comment
	 * @param uid
	 */
	void cancelLikeComment(Long comment, long uid);
}
