package com.chestnut.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.core.IContent;

import lombok.Getter;

@Getter
public class AfterContentPublishEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private IContent<?> content;
	
	public AfterContentPublishEvent(Object source, IContent<?> content) {
		super(source);
		this.content = content;
	}
}
