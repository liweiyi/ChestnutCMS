package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：内容自定义扩展模板
 *
 * <p>此模板会在内容发布时同时发布，可内容独立配置，也可通过栏目配置。</p>
 *
 * 应用场景：
 * 文章内容需要插入轮播图时，可将图集内容设置扩展模板发布成指定格式内容
 * 当不支持ssi时可发布成<script>引用json发布通道的图集数据来实现动态更新
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class PublishPipeProp_ContentExTemplate implements IPublishPipeProp {

	public static final String KEY = "contentExTemplate";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "内容自定义扩展模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog, PublishPipePropUseType.Content);
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY);
		}
		return null;
	}
}
