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
package com.chestnut.cms.stat;

import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.cms.stat.mapper.CmsCatalogContentStatMapper;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 栏目内容数量状态分组统计，5分钟定时更新有变更的栏目数据到数据库
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogContentCountByStatus implements CommandLineRunner {

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final CmsCatalogContentStatMapper catalogContentStatMapper;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private static final Set<Long> changeCatalogIds = new HashSet<>();

    public synchronized void triggerChange(Long catalogId) {
        if (IdUtils.validate(catalogId)) {
            changeCatalogIds.add(catalogId);
        }
    }

    private synchronized Long[] getChangeCatalogIdsAndClear() {
        if (changeCatalogIds.isEmpty()) {
            return null;
        }
        Long[] catalogIds = changeCatalogIds.toArray(Long[]::new);
        changeCatalogIds.clear();
        return catalogIds;
    }

    private void update() {
        Long[] catalogIds = getChangeCatalogIdsAndClear();
        if (Objects.nonNull(catalogIds)) {
            long s = System.currentTimeMillis();
            for (Long catalogId : catalogIds) {
                CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
                if (Objects.isNull(catalog)) {
                    continue;
                }
                boolean insert = false;
                CmsCatalogContentStat stat = this.catalogContentStatMapper.selectById(catalogId);
                if (Objects.isNull(stat)) {
                    stat = new CmsCatalogContentStat();
                    stat.setCatalogId(catalogId);
                    stat.setSiteId(catalog.getSiteId());
                    insert = true;
                }
                List<String> allStatus = ContentStatus.all();
                for (String status : allStatus) {
                    Long total = contentService.dao().lambdaQuery()
                            .eq(CmsContent::getCatalogId, catalogId)
                            .eq(CmsContent::getStatus, status)
                            .count();
                    stat.changeStatusTotal(status, total.intValue());
                }
                if (insert) {
                    this.catalogContentStatMapper.insert(stat);
                } else {
                    this.catalogContentStatMapper.updateById(stat);
                }
            }
            log.info("Stat catalog content by status cost: " + (System.currentTimeMillis() - s) + " ms");
        }
    }

    @PreDestroy
    public void preDestroy() {
        this.update();
    }

    @Override
    public void run(String... args) throws Exception {
        threadPoolTaskScheduler.schedule(this::update, new PeriodicTrigger(Duration.ofSeconds(300)));
    }
}
