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
package com.chestnut.cms.stat.domain.vo;

import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.contentcore.domain.CmsCatalog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * 栏目内容统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ContentStatByCatalogVO {

    /**
     * 栏目ID
     */
    private Long catalogId;

    /**
     * 父级栏目ID
     */
    private Long parentId;

    /**
     * 栏目名称
     */
    private String name;

    /**
     * 初稿内容数量
     */
    private Integer draftTotal = 0;

    /**
     * 待发布内容数量
     */
    private Integer toPublishTotal = 0;

    /**
     * 已发布内容数量
     */
    private Integer publishedTotal = 0;

    /**
     * 已下线内容数量
     */
    private Integer offlineTotal = 0;

    /**
     * 重新编辑内容数量
     */
    private Integer editingTotal = 0;

    public static ContentStatByCatalogVO newInstance(CmsCatalog catalog, CmsCatalogContentStat stat) {
        ContentStatByCatalogVO vo = new ContentStatByCatalogVO();
        vo.setCatalogId(catalog.getCatalogId());
        vo.setParentId(catalog.getParentId());
        vo.setName(catalog.getName());
        if (Objects.nonNull(stat)) {
            vo.setDraftTotal(stat.getDraftTotal());
            vo.setToPublishTotal(stat.getToPublishTotal());
            vo.setPublishedTotal(stat.getPublishedTotal());
            vo.setOfflineTotal(stat.getOfflineTotal());
            vo.setEditingTotal(stat.getEditingTotal());
        }
        return vo;
    }
}
