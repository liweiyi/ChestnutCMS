/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 图集内容资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + ImageContentResourceStat.TYPE)
public class ImageContentResourceStat implements IResourceStat {

    public static final String TYPE = "ImageContent";

    private final IImageService imageService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = imageService.dao().lambdaQuery().select(List.of()).eq(CmsImage::getSiteId, siteId).count();
        while (true) {
            LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>()
                    .select(List.of(CmsImage::getPath))
                    .eq(CmsImage::getSiteId, siteId)
                    .gt(CmsImage::getImageId, lastId)
                    .orderByAsc(CmsImage::getImageId);
            Page<CmsImage> page = imageService.dao().page(new Page<>(0, pageSize, false), q);
            for (CmsImage image : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计图集内容资源引用：" + count + " / " + total + "]");
                lastId = image.getImageId();
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(image.getPath());
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
