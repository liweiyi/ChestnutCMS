package com.chestnut.article;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IPublishPipeProp;

@Component
public class PublishPipeProp_ArticleDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DetailTemplatePropPrefix + ArticleContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "文章详情页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog);
	}
}
