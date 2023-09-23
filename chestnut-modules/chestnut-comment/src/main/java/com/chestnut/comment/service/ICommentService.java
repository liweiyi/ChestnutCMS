package com.chestnut.comment.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.dto.AuditCommentDTO;

public interface ICommentService extends IService<Comment> {
	
	/**
	 * 审核评论
	 */
	void auditComment(AuditCommentDTO dto);

	/**
	 * 删除评论
	 * 
	 * @param commentIds
	 * @return
	 */
	void deleteComments(List<Long> commentIds);
}
