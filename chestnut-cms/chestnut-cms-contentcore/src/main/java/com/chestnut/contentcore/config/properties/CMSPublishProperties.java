/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import java.time.Duration;

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
	 * 启动时清理发布消息队列（默认 false 以避免重启丢失未消费消息，
	 * 如需恢复旧的"重启即清空"行为可显式配置为 true）
	 */
	private boolean clearOnStart = false;

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

	/**
	 * RedisStream 订阅看门狗：兜底检测 subscription 异常（如全部 consumer 长时间未拉消息但 stream 有积压），
	 * 自动 stop/start 监听容器恢复消费。仅在 strategy=RedisStream 时生效。
	 */
	private final Watchdog watchdog = new Watchdog();

	@Getter
	@Setter
	public static class Watchdog {

		/**
		 * 是否启用看门狗
		 */
		private boolean enabled = true;

		/**
		 * 检查间隔
		 */
		private Duration checkInterval = Duration.ofMinutes(1);

		/**
		 * Consumer idleTime 阈值：当所有 consumer 的 idleTime 都超过此值且 stream 有积压时，
		 * 判定订阅已死，执行 stop/start。阈值需明显大于 pollTimeout + 正常 onMessage 处理耗时。
		 */
		private Duration idleTimeThreshold = Duration.ofSeconds(60);

		/**
		 * 每小时最多重启次数，防止根因未除时失控抖动
		 */
		private int maxRestartPerHour = 6;
	}
}
