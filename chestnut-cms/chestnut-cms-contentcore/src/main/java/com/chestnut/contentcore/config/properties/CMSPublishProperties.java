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
@ConfigurationProperties(prefix = "chestnut.cms.publish")
public class CMSPublishProperties {

	/**
	 * 启动时清理发布消息队列
	 */
	private boolean clearOnStart = true;

	/**
	 * 发布消息消费者数量
	 */
	private int consumerCount = 2;

	private final AsyncProperties.Pool pool = new AsyncProperties.Pool();

	private final AsyncProperties.Shutdown shutdown = new AsyncProperties.Shutdown();
}
