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
package com.chestnut.cms.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.service.IImageService;
import com.chestnut.common.async.AsyncTaskManager;
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
 * 图集内容扩展内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCoreDataHandler implements ICoreDataHandler {

    private final IImageService imageService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_image
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出图集内容数据");
        int pageSize = 200;
        long offset = 0;
        int fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>()
                    .eq(CmsImage::getSiteId, context.getSite().getSiteId())
                    .gt(CmsImage::getImageId, offset)
                    .orderByAsc(CmsImage::getImageId);
            Page<CmsImage> page = imageService.dao().page(new Page<>(1, pageSize, false), q);
            if (!page.getRecords().isEmpty()) {
                context.saveData(CmsImage.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        // cms_image
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入图集内容数据");
        List<File> files = context.readDataFiles(CmsImage.TABLE_NAME);
        files.forEach(f -> {
            List<CmsImage> list = JacksonUtils.fromList(f, CmsImage.class);
            for (CmsImage data : list) {
                Long oldImageId = data.getImageId();
                try {
                    data.setImageId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setContentId(context.getContentIdMap().get(data.getContentId()));
                    data.createBy(context.getOperator());
                    data.setPath(context.dealInternalUrl(data.getPath()));
                    imageService.dao().save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入图集内容数据`" + oldImageId + "`失败：" + e.getMessage());
                    log.error("Import cms_image failed: {}", data.getImageId(), e);
                }
            }
        });
    }
}
