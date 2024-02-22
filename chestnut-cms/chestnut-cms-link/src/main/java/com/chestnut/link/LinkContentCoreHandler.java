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
package com.chestnut.link;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.link.service.ILinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 友链内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkContentCoreHandler implements ICoreDataHandler {

    private final ILinkGroupService linkGroupService;

    private final ILinkService linkService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_link_group
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出友情链接数据");
        List<CmsLinkGroup> list = linkGroupService.lambdaQuery()
                .eq(CmsLinkGroup::getSiteId, context.getSite().getSiteId())
                .list();
        context.saveData(CmsLinkGroup.TABLE_NAME, JacksonUtils.to(list));

        List<CmsLink> linkList = linkService.lambdaQuery()
                .eq(CmsLink::getSiteId, context.getSite().getSiteId())
                .list();
        context.saveData(CmsLink.TABLE_NAME, JacksonUtils.to(linkList));
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入友情链接分组数据");
        // cms_link_group
        Map<Long, Long> linkGroupIdMap = new HashMap<>();
        List<File> files = context.readDataFiles(CmsLinkGroup.TABLE_NAME);
        files.forEach(f -> {
            List<CmsLinkGroup> list = JacksonUtils.fromList(f, CmsLinkGroup.class);
            for (CmsLinkGroup data : list) {
                try {
                    Long oldLinkGroupId = data.getLinkGroupId();
                    data.setLinkGroupId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.createBy(context.getOperator());
                    linkGroupService.save(data);
                    linkGroupIdMap.put(oldLinkGroupId, data.getLinkGroupId());
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入友链分组数据失败：" + data.getName() + "[" + data.getCode() + "]");
                    log.error("Import friend link group failed: {}", data.getCode(), e);
                }
            }
        });
        // cms_link
        files = context.readDataFiles(CmsLink.TABLE_NAME);
        files.forEach(f -> {
            List<CmsLink> list = JacksonUtils.fromList(f, CmsLink.class);
            for (CmsLink data : list) {
                try {
                    data.setLinkId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setGroupId(linkGroupIdMap.get(data.getGroupId()));
                    data.createBy(context.getOperator());
                    data.setLogo(context.dealInternalUrl(data.getLogo()));
                    linkService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入友链数据失败：" + data.getName());
                    log.error("Import friend link failed: {}", data.getUrl(), e);
                }
            }
        });
    }
}
