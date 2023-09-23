package com.chestnut.contentcore.listener.event;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsSite;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SiteThemeImportEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private CmsSite site;

	private String destDir;

	private LoginUser operator;

	public SiteThemeImportEvent(Object source, CmsSite site, String destDir, LoginUser operator) {
		super(source);
		this.site = site;
		this.destDir = destDir;
		this.operator = operator;
	}
}
