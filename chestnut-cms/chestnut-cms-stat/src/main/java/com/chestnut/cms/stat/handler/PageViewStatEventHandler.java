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
package com.chestnut.cms.stat.handler;

import com.chestnut.cms.stat.domain.CmsSiteVisitLog;
import com.chestnut.cms.stat.mapper.CmsSiteVisitLogMapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.stat.core.IStatEventHandler;
import com.chestnut.stat.core.StatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 页面浏览事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStatEventHandler.BEAN_PREFIX + PageViewStatEventHandler.TYPE)
public class PageViewStatEventHandler implements IStatEventHandler {

    public static final String TYPE = "pv";

    static final String CACHE_PREFIX = "cms:stat:pv:";

    private final AsyncTaskManager asyncTaskManager;

    private final CmsSiteVisitLogMapper siteVisitLogMapper;

    private final ContentDynamicDataService contentDynamicDataService;

    private final RedisCache redisCache;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void handle(StatEvent event) {
        CmsSiteVisitLog log = parseSiteVisitLog(event);
        // 更新Redis数据
        this.redisCache.incrLongCounter(CACHE_PREFIX + log.getUri());
        // 更新内容浏览量
        if (IdUtils.validate(log.getContentId())) {
            contentDynamicDataService.increaseViewCount(log.getContentId());
        }
        asyncTaskManager.execute(() -> siteVisitLogMapper.insert(log));
    }

    private CmsSiteVisitLog parseSiteVisitLog(StatEvent event) {
        // 记录PV日志
        CmsSiteVisitLog log = new CmsSiteVisitLog();
        log.setLogId(IdUtils.getSnowflakeId());
        log.setSiteId(event.getData().get("sid").asLong());
        log.setCatalogId(event.getData().get("cid").asLong(0));
        log.setContentId(event.getData().get("id").asLong(0));

        log.setHost(event.getRequestData().getHost());
        log.setUri(event.getRequestData().getUri());
        log.setIp(event.getRequestData().getIp());
        log.setAddress(event.getRequestData().getAddress());
        log.setReferer(event.getRequestData().getReferer());
        log.setLocale(event.getRequestData().getLocale());

        log.setUserAgent(event.getRequestData().getUserAgent());
        log.setBrowser(event.getRequestData().getBrowser());
        log.setOs(event.getRequestData().getOs());
        log.setDeviceType(event.getRequestData().getDeviceType());

        log.setEvtTime(event.getEvtTime());
        return log;
    }
}
