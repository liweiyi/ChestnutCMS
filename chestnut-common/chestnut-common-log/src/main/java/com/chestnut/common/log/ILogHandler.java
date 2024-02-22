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
package com.chestnut.common.log;

/**
 * 日志处理器
 */
public interface ILogHandler {
	
	/**
	 * 是否处理指定日志类型
	 * 
	 * @param logType 日志类型
	 * @return 结果
	 */
	boolean test(String logType);

	/**
	 * 处理日志
	 * 
	 * @param logDetail 日志详情
	 */
	void handler(LogDetail logDetail);
}
