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
package com.chestnut.advertisement.stat;

import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.service.IAdvertisementStatService;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.stat.core.IStatEventHandler;
import com.chestnut.stat.core.StatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 广告点击事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStatEventHandler.BEAN_PREFIX + AdClickStatEventHandler.TYPE)
public class AdClickStatEventHandler implements IStatEventHandler {

    public static final String TYPE = "adclick";

    private final IAdvertisementStatService advertisementStatService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void handle(StatEvent event) {
        CmsAdClickLog log = parseLog(event);
        advertisementStatService.adClick(log);
    }

    private CmsAdClickLog parseLog(StatEvent event) {
        CmsAdClickLog log = new CmsAdClickLog();
        log.setLogId(IdUtils.getSnowflakeId());
        log.setSiteId(event.getData().get("sid").asLong());
        log.setAdId(event.getData().get("aid").asLong());

        log.setHost(event.getRequestData().getHost());
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
