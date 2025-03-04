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
package com.chestnut.advertisement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义区块资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + AdvResourceStat.TYPE)
public class AdvResourceStat implements IResourceStat {

    public static final String TYPE = "Adv";

    private final IAdvertisementService advertisementService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = advertisementService.lambdaQuery().select(List.of(CmsAdvertisement::getAdvertisementId))
                .eq(CmsAdvertisement::getSiteId, siteId).count();
        while (true) {
            LambdaQueryWrapper<CmsAdvertisement> q = new LambdaQueryWrapper<CmsAdvertisement>()
                    .select(List.of(CmsAdvertisement::getResourcePath))
                    .eq(CmsAdvertisement::getSiteId, siteId)
                    .gt(CmsAdvertisement::getAdvertisementId, lastId)
                    .orderByAsc(CmsAdvertisement::getAdvertisementId);
            Page<CmsAdvertisement> page = advertisementService.page(new Page<>(0, pageSize, false), q);
            for (CmsAdvertisement advertisement : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计广告资源引用：" + count + " / " + total + "]");
                lastId = advertisement.getAdvertisementId();
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(advertisement.getResourcePath());
                if (Objects.nonNull(internalURL)) {
                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }
}
