/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.cms.stat.baidu;

import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

@Slf4j
public class BaiduTongjiUtils {

	/**
	 * 刷新AccessToken
	 */
	private static final String API_REFRESH_ACCESS_TOKEN = "http://openapi.baidu.com/oauth/2.0/token?grant_type=refresh_token&refresh_token={0}&client_id={1}&client_secret={2}";

	/**
	 * 刷新AccessToken
	 * 
	 * @param config
	 * @return
	 */
	public static Map<String, Object> refreshAccessToken(BaiduTongjiConfig config) {
		String url = StringUtils.messageFormat(API_REFRESH_ACCESS_TOKEN, config.getRefreshToken(),
				config.getApiKey(), config.getSecretKey());
		String result = HttpUtils.get(URI.create(url));
		return JacksonUtils.fromMap(result);
	}
}
