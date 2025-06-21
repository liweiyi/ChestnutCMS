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
package com.chestnut.article.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.article.domain.BCmsArticleDetail;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.mapper.BCmsArticleDetailMapper;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.common.db.mybatisplus.BackupServiceImpl;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ArticleDetailDAO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class CmsArticleDetailDAO extends BackupServiceImpl<CmsArticleDetailMapper, CmsArticleDetail, BCmsArticleDetailMapper, BCmsArticleDetail> {

    public long countBySiteId(Long siteId) {
        if (!IdUtils.validate(siteId)) {
            return 0;
        }
        return this.count(new LambdaQueryWrapper<CmsArticleDetail>()
                .eq(CmsArticleDetail::getSiteId, siteId));
    }

    public Page<CmsArticleDetail> pageBySiteId(Page<CmsArticleDetail> page, Long siteId, List<SFunction<CmsArticleDetail, ?>> columns) {
        if (!IdUtils.validate(siteId)) {
            return page;
        }
        LambdaQueryWrapper<CmsArticleDetail> q = new LambdaQueryWrapper<CmsArticleDetail>()
                .select(StringUtils.isNotEmpty(columns), columns)
                .eq(CmsArticleDetail::getSiteId, siteId);
        return this.page(page, q);
    }

    public long countBackupBySiteId(Long siteId) {
        if (!IdUtils.validate(siteId)) {
            return 0;
        }
        return this.getBackupMapper().selectCount(new LambdaQueryWrapper<BCmsArticleDetail>()
                .eq(BCmsArticleDetail::getSiteId, siteId));
    }

    public Page<BCmsArticleDetail> pageBackupBySiteId(Page<BCmsArticleDetail> page, Long siteId, List<SFunction<BCmsArticleDetail, ?>> columns) {
        if (!IdUtils.validate(siteId)) {
            return page;
        }
        return this.getBackupMapper().selectPage(page, new LambdaQueryWrapper<BCmsArticleDetail>()
                .select(StringUtils.isNotEmpty(columns), columns)
                .eq(BCmsArticleDetail::getSiteId, siteId));
    }

    public void removeBackupBatchByIds(List<Long> backupIds) {
        if (StringUtils.isEmpty(backupIds)) {
            return;
        }
        this.getBackupMapper().deleteByIds(backupIds);
    }
}
