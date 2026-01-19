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
package com.chestnut.common.db.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.db.DBConstants;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * IBackupService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IBackupService<T, B> extends IService<T> {

    BaseMapper<B> getBackupMapper();

    B getBackupById(Long backupEntityId);

    List<B> getBackupByIds(Collection<Long> backupIds);

    B getOneBackup(LambdaQueryWrapper<B> wrapper);

    void backup(T entity, String operator, String backupRemark);

    default void deleteByIdAndBackup(T entity, String operator) {
        deleteByIdAndBackup(entity, operator, DBConstants.BACKUP_REMARK_DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    void deleteByIdAndBackup(T entity, String operator, String backupRemark);

    default void deleteAndBackup(Wrapper<T> wrapper, String operator) {
        deleteAndBackup(wrapper, operator, DBConstants.BACKUP_REMARK_DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    void deleteAndBackup(Wrapper<T> wrapper, String operator, String backupRemark);

    void recover(Serializable backupEntityId);

    void recover(B backupEntity);

    void deleteBackupById(B backupEntity);

    void deleteBackupById(Serializable backupId);

    void deleteBackupByIds(Collection<Serializable> backupIds);

    void deleteBackups(Wrapper<B> wrapper);
}
