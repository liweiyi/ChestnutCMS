package com.chestnut.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 读取项目相关配置
 *
 * @author 兮玥（190785909@qq.com）
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut")
public class ChestnutProperties {
	
	/** 项目名称 */
	private String name;

	/** 版本 */
	private String version;

	/** 版权年份 */
	private String copyrightYear;
	
	/**
	 * 雪花算法参数配置
	 */
	private Snowflake snowflake;

	public static class Snowflake {

		private short workerId = 1;

		public short getWorkerId() {
			return workerId;
		}

		public void setWorkerId(short workerId) {
			this.workerId = workerId;
		}
	}
}
