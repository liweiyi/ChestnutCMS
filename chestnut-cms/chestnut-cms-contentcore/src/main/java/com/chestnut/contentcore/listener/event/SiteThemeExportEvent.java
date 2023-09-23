package com.chestnut.contentcore.listener.event;

import com.chestnut.contentcore.domain.CmsSite;
import jodd.io.ZipBuilder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SiteThemeExportEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private CmsSite site;

	private ZipBuilder zipBuilder;

	public SiteThemeExportEvent(Object source, CmsSite site, ZipBuilder zipBuilder) {
		super(source);
		this.site = site;
		this.zipBuilder = zipBuilder;
	}
}
