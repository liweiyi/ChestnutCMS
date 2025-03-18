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
package com.chestnut.cms.image.dao;

import com.chestnut.cms.image.domain.BCmsImage;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.mapper.BCmsImageMapper;
import com.chestnut.cms.image.mapper.CmsImageMapper;
import com.chestnut.common.db.DBConstants;
import com.chestnut.common.db.mybatisplus.BackupServiceImpl;
import com.chestnut.common.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CmsImageDAO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class CmsImageDAO extends BackupServiceImpl<CmsImageMapper, CmsImage, BCmsImageMapper, BCmsImage> {

    private final BCmsImageDAO backupDAO;

    @Transactional(rollbackFor = Exception.class)
    public void deleteByContentIdAndBackup(Long contentId, String operator) {
        List<CmsImage> entities = this.lambdaQuery()
                .eq(CmsImage::getContentId, contentId)
                .list();
        List<BCmsImage> backupEntities = entities.stream()
                .map(entity -> {
                    BCmsImage backupEntity = entity.toBackupEntity();
                    backupEntity.setBackupId(IdUtils.getSnowflakeId());
                    backupEntity.setBackupTime(LocalDateTime.now());
                    backupEntity.setBackupBy(operator);
                    backupEntity.setBackupRemark(DBConstants.BACKUP_REMARK_DELETE);
                    return backupEntity;
                })
                .toList();
        this.backupDAO.saveBatch(backupEntities);
        this.removeByIds(entities);
    }
}
