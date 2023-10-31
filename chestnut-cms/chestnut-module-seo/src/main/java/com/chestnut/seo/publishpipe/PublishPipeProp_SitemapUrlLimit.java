package com.chestnut.seo.publishpipe;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：站点地图单文件url数量限制
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class PublishPipeProp_SitemapUrlLimit implements IPublishPipeProp {
	
	public static final String KEY = "sitemapUrlLimit";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "站点地图单文件url数量限制";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	public static int getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			String value = MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY);
			if (NumberUtils.isDigits(value)) {
				return Integer.parseInt(value);
			}
		}
		return 30000;
	}
}
