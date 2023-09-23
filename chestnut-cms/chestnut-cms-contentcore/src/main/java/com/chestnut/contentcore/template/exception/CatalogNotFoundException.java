package com.chestnut.contentcore.template.exception;

import com.chestnut.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class CatalogNotFoundException extends TemplateException {

	private static final long serialVersionUID = 1L;

	public CatalogNotFoundException(String tag, long catalogId, String alias, Environment env) {
		super(StringUtils.messageFormat("<@{0}>[id: {1}, alias: {2}]", tag, catalogId, alias), env);
	}
}
