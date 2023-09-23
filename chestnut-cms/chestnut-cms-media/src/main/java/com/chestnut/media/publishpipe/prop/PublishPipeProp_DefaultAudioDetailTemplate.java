package com.chestnut.media.publishpipe.prop;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.media.AudioContentType;

@Component
public class PublishPipeProp_DefaultAudioDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DefaultDetailTemplatePropPrefix + AudioContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "音频集详情页默认模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}
}
