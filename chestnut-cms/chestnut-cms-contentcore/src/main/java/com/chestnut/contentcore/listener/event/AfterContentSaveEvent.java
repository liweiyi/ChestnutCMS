package com.chestnut.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.core.IContent;

import lombok.Getter;

@Getter
public class AfterContentSaveEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private IContent<?> content;

	private boolean add;
	
	public AfterContentSaveEvent(Object source, IContent<?> content, boolean add) {
		super(source);
		this.content = content;
		this.add = add;
	}

	public boolean isAdd() {
		return add;
	}
}
