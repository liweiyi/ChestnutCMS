package com.chestnut.cms.search.publishpipe;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PublishPipeProp_SearchTemplate implements IPublishPipeProp {

	public static final String KEY = "searchTemplate";

	static final String DEFAULT_VALUE = "search.template.html";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "搜索结果页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	@Override
	public String getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY, DEFAULT_VALUE);
		}
		return null;
	}
}
