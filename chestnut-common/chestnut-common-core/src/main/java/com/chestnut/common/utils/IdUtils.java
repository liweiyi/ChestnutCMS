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
package com.chestnut.common.utils;

import com.chestnut.common.config.properties.ChestnutProperties;
import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ID生成器工具类
 *
 * @author 兮玥（190785909@qq.com）
 */
public class IdUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);
	
	static {
		ChestnutProperties properties = SpringUtils.getBean(ChestnutProperties.class);
		ChestnutProperties.Snowflake snowflake = properties.getSnowflake();
		short workerId = Objects.isNull(snowflake) ? 1 : snowflake.getWorkerId();
		IdGeneratorOptions options = new IdGeneratorOptions(workerId);
		YitIdHelper.setIdGenerator(options);
		logger.info("Snowflake: workerId={}", workerId);
	}
	
	public static long getSnowflakeId() {
		return YitIdHelper.nextId();
	}

	public static String getSnowflakeIdStr() {
		return String.valueOf(getSnowflakeId());
	}
	
	/**
	 * 获取随机UUID
	 * 
	 * @return 随机UUID
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString());
	}

	/**
	 * 简化的UUID，去掉了横线
	 * 
	 * @return 简化的UUID，去掉了横线
	 */
	public static String simpleUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 校验列表中的ID是否正确
	 * 
	 * @param ids
	 * @param removeInvalidId
	 *            是否移除错误ID
	 * @return
	 */
	public static boolean validate(List<Long> ids, boolean removeInvalidId) {
		if (ids == null || ids.size() == 0) {
			return false;
		}
		for (Iterator<Long> iterator = ids.iterator(); iterator.hasNext();) {
			Long id = iterator.next();
			if (id == null || id <= 0) {
				if (removeInvalidId) {
					iterator.remove();
				} else {
					return false;
				}
			}
		}
		return ids.size() > 0;
	}

	public static boolean validate(List<Long> ids) {
		return validate(ids, false);
	}

	public static boolean validate(Long id) {
		return id != null && id > 0;
	}
}
