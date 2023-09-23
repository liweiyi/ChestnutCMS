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
