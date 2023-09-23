package com.chestnut.contentcore.listener.event;

import com.chestnut.contentcore.core.IContent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AfterContentToPublishEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private IContent<?> content;

	public AfterContentToPublishEvent(Object source, IContent<?> content) {
		super(source);
		this.content = content;
	}
}
