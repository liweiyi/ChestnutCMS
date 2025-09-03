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
package com.chestnut.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.search.domain.SearchLog;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.mapper.SearchLogMapper;
import com.chestnut.search.service.ISearchLogService;
import com.chestnut.search.service.ISearchWordHourStatService;
import com.chestnut.search.service.ISearchWordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class SearchLogServiceImpl extends ServiceImpl<SearchLogMapper, SearchLog> implements ISearchLogService {

	private final AsyncTaskManager asyncTaskManager;

	private final ISearchWordService searchWordStatService;

	private final ISearchWordHourStatService searchWordHourStatService;

	@Override
	public void addSearchLog(String source, String query, HttpServletRequest request) {
		final String userAgent = ServletUtils.getUserAgent(request);
		final String ip = ServletUtils.getIpAddr(request);
		final String referer = ServletUtils.getReferer(request);
		final LocalDateTime logTime = LocalDateTime.now();
		asyncTaskManager.execute(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setLogId(IdUtils.getSnowflakeId());
			sLog.setWord(query);
			sLog.setIp(ip);
			sLog.setLocation(IP2RegionUtils.ip2Region(ip));
			sLog.setLogTime(logTime);
			sLog.setUserAgent(userAgent);
			sLog.setReferer(referer);
			sLog.setClientType(ServletUtils.getDeviceType(userAgent));
			sLog.setSource(source);
			this.save(sLog);

			SearchWord searchWord = searchWordStatService.getSearchWord(sLog.getWord(), sLog.getSource());
			searchWordStatService.increaseSearchCount(searchWord);
			searchWordHourStatService.handleSearchLog(searchWord, sLog.getLogTime());
		});
	}
}