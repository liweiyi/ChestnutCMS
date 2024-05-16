/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.contentcore.config.properties;

import com.chestnut.common.config.properties.AsyncProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CMS发布配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CMSPublishProperties.PREFIX)
public class CMSPublishProperties {

	public static final String PREFIX = "chestnut.cms.publish";

	/**
	 * 启动时清理发布消息队列
	 */
	private boolean clearOnStart = true;

	/**
	 * 发布消息消费者数量
	 */
	private int consumerCount = 2;

	/**
	 * 发布策略
	 */
	private String strategy;

	private final AsyncProperties.Pool pool = new AsyncProperties.Pool();

	private final AsyncProperties.Shutdown shutdown = new AsyncProperties.Shutdown();
}
