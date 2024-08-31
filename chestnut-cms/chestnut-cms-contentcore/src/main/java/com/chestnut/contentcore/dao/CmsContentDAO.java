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
package com.chestnut.contentcore.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.db.mybatisplus.BackupServiceImpl;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.mapper.BCmsContentMapper;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * ContentDAO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class CmsContentDAO extends BackupServiceImpl<CmsContentMapper, CmsContent, BCmsContentMapper, BCmsContent> {

    public final List<BCmsContent> listBackupContentByIds(@NotNull Collection<Long> backupIds,
                                                          List<SFunction<BCmsContent, ?>> columns) {
        if (backupIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<BCmsContent> q = new LambdaQueryWrapper<BCmsContent>()
            .select(StringUtils.isNotEmpty(columns), columns)
            .in(BCmsContent::getBackupId, backupIds);
        return this.getBackupMapper().selectList(q);
    }

    public long countBySiteId(Long siteId) {
        if (IdUtils.validate(siteId)) {
            return 0;
        }
        return this.count(new LambdaQueryWrapper<CmsContent>()
                .eq(CmsContent::getSiteId, siteId));
    }

    public Page<CmsContent> pageBySiteId(Page<CmsContent> page, Long siteId, List<SFunction<CmsContent, ?>> columns) {
        if (IdUtils.validate(siteId)) {
            return page;
        }
        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
                .select(StringUtils.isNotEmpty(columns), columns)
                .eq(CmsContent::getSiteId, siteId);
        return this.page(page, q);
    }

    public long countBackupBySiteId(Long siteId) {
        if (IdUtils.validate(siteId)) {
            return 0;
        }
        return this.getBackupMapper().selectCount(new LambdaQueryWrapper<BCmsContent>()
                .eq(BCmsContent::getSiteId, siteId));
    }

    public Page<BCmsContent> pageBackupBySiteId(Page<BCmsContent> page, Long siteId, List<SFunction<BCmsContent, ?>> columns) {
        if (IdUtils.validate(siteId)) {
            return page;
        }
        return this.getBackupMapper().selectPage(page, new LambdaQueryWrapper<BCmsContent>()
                .select(StringUtils.isNotEmpty(columns), columns)
                .eq(BCmsContent::getSiteId, siteId));
    }

    public void removeBackupBatchByIds(List<Long> backupIds) {
        if (StringUtils.isEmpty(backupIds)) {
            return;
        }
        this.getBackupMapper().deleteBatchIds(backupIds);
    }
}
