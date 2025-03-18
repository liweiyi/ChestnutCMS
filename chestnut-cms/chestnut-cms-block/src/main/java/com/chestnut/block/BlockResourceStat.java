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
package com.chestnut.block;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
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
@Component(IResourceStat.BEAN_PREFIX + BlockResourceStat.TYPE)
public class BlockResourceStat implements IResourceStat {

    public static final String TYPE = "Block";

    private final IPageWidgetService pageWidgetService;

    private final ManualPageWidgetType manualPageWidgetType;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = pageWidgetService.lambdaQuery().select(List.of(CmsPageWidget::getContent))
                .eq(CmsPageWidget::getSiteId, siteId).eq(CmsPageWidget::getType, manualPageWidgetType.getId()).count();
        while (true) {
            LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
                    .select(List.of(CmsPageWidget::getContent))
                    .eq(CmsPageWidget::getSiteId, siteId)
                    .eq(CmsPageWidget::getType, manualPageWidgetType.getId())
                    .gt(CmsPageWidget::getPageWidgetId, lastId)
                    .orderByAsc(CmsPageWidget::getPageWidgetId);
            Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(0, pageSize, false), q);
            for (CmsPageWidget pageWidget : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计自定义区块资源引用：" + count + " / " + total + "]");
                lastId = pageWidget.getPageWidgetId();
                if (StringUtils.isNotEmpty(pageWidget.getContent())) {
                    List<ManualPageWidgetType.RowData> rowData = JacksonUtils.fromList(pageWidget.getContent(), ManualPageWidgetType.RowData.class);
                    if (Objects.nonNull(rowData)) {
                        rowData.forEach(row -> {
                            row.getItems().forEach(item -> {
                                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(item.getLogo());
                                if (Objects.nonNull(internalURL)) {
                                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                                }
                            });
                        });
                    }
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
