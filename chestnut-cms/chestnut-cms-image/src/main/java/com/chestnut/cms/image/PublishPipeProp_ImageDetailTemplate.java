package com.chestnut.cms.image;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IPublishPipeProp;

@Component
public class PublishPipeProp_ImageDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DetailTemplatePropPrefix + ImageContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "图片集详情页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog);
	}
}
