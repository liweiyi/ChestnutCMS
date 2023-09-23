package com.chestnut.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.domain.CmsSite;

import lombok.Getter;

@Getter
public class AfterSiteDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	
	private CmsSite site;
	
	public AfterSiteDeleteEvent(Object source, CmsSite site) {
		super(source);
		this.site = site;
	}
}
