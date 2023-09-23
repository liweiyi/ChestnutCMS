package com.chestnut.comment.listener.event;

import com.chestnut.comment.domain.Comment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 评论保存数据库前事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class BeforeCommentSubmitEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private Comment comment;

	public BeforeCommentSubmitEvent(Object source, Comment comment) {
		super(source);
		this.comment = comment;
	}
}
