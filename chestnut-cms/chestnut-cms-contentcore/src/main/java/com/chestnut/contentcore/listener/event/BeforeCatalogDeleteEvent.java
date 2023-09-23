package com.chestnut.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class BeforeCatalogDeleteEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private CmsCatalog catalog;
	
	private LoginUser operator;
	
	public BeforeCatalogDeleteEvent(Object source, CmsCatalog catalog, LoginUser operator) {
		super(source);
		this.catalog = catalog;
		this.operator = operator;
	}
}
