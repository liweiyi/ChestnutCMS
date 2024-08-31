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
package com.chestnut.cms.dynamic;

import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import com.chestnut.cms.dynamic.service.IDynamicPageService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 自定义动态模板核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicCoreHandler implements ICoreDataHandler {

    private final IDynamicPageService dynamicPageService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_dynamic_page
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出自定义动态模板页面数据");
        List<CmsDynamicPage> list = dynamicPageService.lambdaQuery()
                .eq(CmsDynamicPage::getSiteId, context.getSite().getSiteId())
                .list();
        context.saveData(CmsDynamicPage.TABLE_NAME, JacksonUtils.to(list));
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入自定义动态模板页面数据");
        // cms_dynamic_page
        List<File> files = context.readDataFiles(CmsDynamicPage.TABLE_NAME);
        files.forEach(f -> {
            List<CmsDynamicPage> list = JacksonUtils.fromList(f, CmsDynamicPage.class);
            for (CmsDynamicPage data : list) {
                try {
                    Long count = dynamicPageService.lambdaQuery().eq(CmsDynamicPage::getPath, data.getPath()).count();
                    Assert.isTrue(count == 0, () -> new GlobalException("自定义动态模板路径重复：" + data.getPath()));

                    data.setPageId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.createBy(context.getOperator());
                    dynamicPageService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入自定义动态模板页面失败：" + data.getName() + "[" + data.getCode() + "]");
                    log.error("Import dynamic page failed: {}", data.getCode(), e);
                }
            }
        });
    }
}
