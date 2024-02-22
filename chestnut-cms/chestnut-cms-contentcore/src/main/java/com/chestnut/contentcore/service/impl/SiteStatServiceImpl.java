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
package com.chestnut.contentcore.service.impl;

import java.util.Map;
import java.util.stream.Collectors;

import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.service.ITemplateService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.contentcore.domain.vo.SiteStatVO;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.mapper.CmsResourceMapper;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.ISiteStatService;
import com.chestnut.contentcore.util.ContentCoreUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Service
public class SiteStatServiceImpl implements ISiteStatService {

    private final ICatalogService catalogService;

    private final CmsContentMapper contentMapper;

    private final CmsResourceMapper resourceMapper;

    private final ITemplateService templateService;

    /**
     * 获取站点相关资源统计数据
     *
     * @param site
     * @return
     */
    @Override
    public SiteStatVO getSiteStat(CmsSite site) {
        SiteStatVO vo = new SiteStatVO();
        vo.setSiteId(site.getSiteId());

        // 栏目数量
        long catalogCount = this.catalogService
                .count(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
        vo.setCatalogCount(catalogCount);
        // 内容数量
        long contentCount = this.contentMapper
                .selectCount(new LambdaQueryWrapper<CmsContent>().eq(CmsContent::getSiteId, site.getSiteId()));
        vo.setContentCount(contentCount);
        // 内容分类统计
        Map<String, Long> countContentMap = this.contentMapper.countContentGroupByType(site.getSiteId()).stream()
                .collect(Collectors.toMap(SiteStatData::getDataKey, SiteStatData::getDataValue));
        ContentCoreUtils.getContentTypes().values().forEach(ct -> {
            if (!countContentMap.containsKey(ct.getId())) {
                countContentMap.put(ct.getId(), 0L);
            }
        });
        vo.setContentDetails(countContentMap);
        // 资源数量
        long resourceCount = this.resourceMapper
                .selectCount(new LambdaQueryWrapper<CmsResource>().eq(CmsResource::getSiteId, site.getSiteId()));
        vo.setResourceCount(resourceCount);
        // 资源分类统计
        Map<String, Long> countResourceMap = this.resourceMapper.countResourceGroupByType(site.getSiteId()).stream()
                .collect(Collectors.toMap(SiteStatData::getDataKey, SiteStatData::getDataValue));
        ContentCoreUtils.getResourceTypes().forEach(rt -> {
            if (!countResourceMap.containsKey(rt.getId())) {
                countResourceMap.put(rt.getId(), 0L);
            }
        });
        vo.setResourceDetails(countResourceMap);
        // 模板数量
        long templateCount = this.templateService.count(new LambdaQueryWrapper<CmsTemplate>()
                .eq(CmsTemplate::getSiteId, site.getSiteId()));
        vo.setTemplateCount(templateCount);
        return vo;
    }

    @Getter
    @Setter
    public static class SiteStatData {

        private String dataKey;

        private Long dataValue;
    }
}
