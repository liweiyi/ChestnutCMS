package com.chestnut.comment.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.comment.domain.Comment;

import lombok.Getter;

/**
 * 评论保存数据库后事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class AfterCommentSubmitEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private Comment comment;

	public AfterCommentSubmitEvent(Object source, Comment comment) {
		super(source);
		this.comment = comment;
	}
}
