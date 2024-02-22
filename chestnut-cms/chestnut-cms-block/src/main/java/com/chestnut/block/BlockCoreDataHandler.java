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
package com.chestnut.block;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 自定义区块页面部件内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class BlockCoreDataHandler implements ICoreDataHandler {

    private final IPageWidgetService pageWidgetService;

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在处理自定义区块详情数据");
        List<CmsPageWidget> pageWidgets = this.pageWidgetService.lambdaQuery()
                .eq(CmsPageWidget::getSiteId, context.getSite().getSiteId())
                .eq(CmsPageWidget::getType, ManualPageWidgetType.ID)
                .list();
        pageWidgets.forEach(pageWidget -> {
            if (StringUtils.isNotEmpty(pageWidget.getContent())) {
                List<ManualPageWidgetType.RowData> rowData = JacksonUtils
                        .fromList(pageWidget.getContent(), ManualPageWidgetType.RowData.class);
                if (Objects.nonNull(rowData)) {
                    rowData.forEach(row -> {
                        row.getItems().forEach(item -> {
                            // 处理iurl
                            item.setLogo(context.dealInternalUrl(item.getLogo()));
                            item.setUrl(context.dealInternalUrl(item.getUrl()));
                        });
                    });
                }
                pageWidget.setContent(JacksonUtils.to(rowData));
            }
        });
        pageWidgetService.updateBatchById(pageWidgets);
    }
}
