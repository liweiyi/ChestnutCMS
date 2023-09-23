package com.chestnut.contentcore.listener.event;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class AfterCatalogSaveEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private String oldPath;

	private Map<String, Object> extendParams;

	private CmsCatalog catalog;

	public AfterCatalogSaveEvent(Object source, CmsCatalog catalog, String oldPath, Map<String, Object> extendParams) {
		super(source);
		this.catalog = catalog;
		this.oldPath = oldPath;
		this.extendParams = extendParams;
	}
}
