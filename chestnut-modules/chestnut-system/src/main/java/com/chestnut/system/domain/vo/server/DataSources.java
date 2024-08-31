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
package com.chestnut.system.domain.vo.server;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class DataSources {

	private List<DataSource> list = new ArrayList<>();

	public void initFrom(Collection<DataSourceProperty> properties) {
		properties.forEach(property -> {
			DataSource dataSource = new DataSource();
			dataSource.poolName = property.getPoolName();
			dataSource.driverClass = property.getDriverClassName();
			dataSource.url = property.getUrl();
			dataSource.username = property.getUsername();
			list.add(dataSource);
		});
	}

	@Getter
	@Setter
	public static class DataSource {

		/**
		 * 数据源
		 */
		private String poolName;

		/**
		 * 驱动类
		 */
		private String driverClass;

		/**
		 * 数据库连接
		 */
		private String url;

		/**
		 * 用户名
		 */
		private String username;
	}
}