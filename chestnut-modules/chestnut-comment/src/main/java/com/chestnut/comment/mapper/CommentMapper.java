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
package com.chestnut.comment.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.comment.domain.Comment;

/**
 * <p>
 * 评论Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface CommentMapper extends BaseMapper<Comment> {

	/**
	 * 评论回复数+1
	 * 
	 * @param commentId
	 * @return
	 */
	@Update("UPDATE " + Comment.TABLE_NAME + " SET reply_count = reply_count + 1 WHERE comment_id = #{commentId}")
	public int incrCommentReplyCount(@Param("commentId") Long commentId);

	/**
	 * 评论回复数-1
	 * 
	 * @param commentId
	 * @return
	 */
	@Update("UPDATE " + Comment.TABLE_NAME + " SET reply_count = reply_count - 1 WHERE comment_id = #{commentId}")
	public int decrCommentReplyCount(@Param("commentId") Long commentId);

	/**
	 * 评论点赞数+1
	 * 
	 * @param commentId
	 * @return
	 */
	@Update("UPDATE " + Comment.TABLE_NAME + " SET like_count = like_count + 1 WHERE comment_id = #{commentId}")
	public int incrCommentLikeCount(@Param("commentId") Long commentId);

	/**
	 * 评论点赞数-1
	 * 
	 * @param commentId
	 * @return
	 */
	@Update("UPDATE " + Comment.TABLE_NAME + " SET like_count = like_count - 1 WHERE comment_id = #{commentId}")
	public int decrCommentLikeCount(@Param("commentId") Long commentId);
}