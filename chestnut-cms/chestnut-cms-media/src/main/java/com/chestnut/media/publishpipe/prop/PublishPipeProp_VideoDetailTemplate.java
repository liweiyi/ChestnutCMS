package com.chestnut.media.publishpipe.prop;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.media.VideoContentType;

@Component
public class PublishPipeProp_VideoDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DetailTemplatePropPrefix + VideoContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "视频集详情页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog);
	}
}
