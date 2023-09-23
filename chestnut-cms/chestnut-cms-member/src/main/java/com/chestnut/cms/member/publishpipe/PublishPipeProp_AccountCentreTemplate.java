package com.chestnut.cms.member.publishpipe;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PublishPipeProp_AccountCentreTemplate implements IPublishPipeProp {

	public static final String KEY = "accountCentreTemplate";

	static final String DEFAULT_VALUE = "account/account_centre.template.html";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "个人中心页模板";
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
		return DEFAULT_VALUE;
	}
}
