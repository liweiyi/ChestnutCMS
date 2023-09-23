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