package com.chestnut.search.service.impl;

import java.time.LocalDateTime;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.search.domain.SearchLog;
import com.chestnut.search.domain.dto.SearchLogDTO;
import com.chestnut.search.mapper.SearchLogMapper;
import com.chestnut.search.service.ISearchLogService;

@RequiredArgsConstructor
@Service
public class SearchLogServiceImpl extends ServiceImpl<SearchLogMapper, SearchLog> implements ISearchLogService {

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public void addSearchLog(SearchLogDTO dto) {
		asyncTaskManager.execute(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setWord(dto.getWord());
			sLog.setIp(dto.getIp());
			sLog.setLocation(IP2RegionUtils.ip2Region(dto.getIp()));
			sLog.setLogTime(dto.getLogTime());
			sLog.setUserAgent(dto.getUserAgent());
			sLog.setReferer(dto.getReferer());
			sLog.setSource(dto.getSource());
			sLog.setClientType(ServletUtils.getDeviceType(dto.getUserAgent()));
			this.save(sLog);
		});
	}

	@Override
	public void addSearchLog(String source, String query, HttpServletRequest request) {
		final String userAgent = ServletUtils.getUserAgent(request);
		final String ip = ServletUtils.getIpAddr(request);
		final String referer = ServletUtils.getReferer(request);
		final LocalDateTime logTime = LocalDateTime.now();
		asyncTaskManager.execute(() -> {
			SearchLog sLog = new SearchLog();
			sLog.setWord(query);
			sLog.setIp(ip);
			sLog.setLocation(IP2RegionUtils.ip2Region(ip));
			sLog.setLogTime(logTime);
			sLog.setUserAgent(userAgent);
			sLog.setReferer(referer);
			sLog.setClientType(ServletUtils.getDeviceType(userAgent));
			sLog.setSource(source);
			this.save(sLog);
		});
	}
}