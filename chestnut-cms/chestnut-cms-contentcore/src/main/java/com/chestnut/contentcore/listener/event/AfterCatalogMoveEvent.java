package com.chestnut.contentcore.listener.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class AfterCatalogMoveEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private CmsCatalog fromCatalog;
	
	private CmsCatalog toCatalog;
	
	private List<CmsCatalog> childrenCatalogs;
	
	public AfterCatalogMoveEvent(Object source, CmsCatalog fromCatalog, CmsCatalog toCatalog, List<CmsCatalog> children) {
		super(source);
		this.fromCatalog = fromCatalog;
		this.toCatalog = toCatalog;
		this.childrenCatalogs = children;
	}
}
