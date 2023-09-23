package com.chestnut.contentcore.template.impl;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.template.ITemplateType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + SiteTemplateType.TypeId)
public class SiteTemplateType implements ITemplateType {
	
	public final static String TypeId = "SiteIndex";

	@Override
	public String getId() {
		return TypeId;
	}
}
