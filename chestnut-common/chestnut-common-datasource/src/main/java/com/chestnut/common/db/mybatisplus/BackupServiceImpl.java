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
package com.chestnut.common.db.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.db.domain.IBackupEntity;
import com.chestnut.common.db.domain.IBackupable;
import com.chestnut.common.utils.IdUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * IBackupService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class BackupServiceImpl<M extends BaseMapper<T>, T extends IBackupable<B>,
        MB extends BaseMapper<B>, B extends IBackupEntity<T>> extends ServiceImpl<M, T> implements IBackupService<T, B> {

    @Autowired
    private MB backupMapper;

    @Override
    public MB getBackupMapper() {
        return backupMapper;
    }

    @Override
    public B getBackupById(Long backupEntityId) {
        return backupMapper.selectById(backupEntityId);
    }

    @Override
    public List<B> getBackupByIds(Collection<Long> backupIds) {
        return backupMapper.selectBatchIds(backupIds);
    }

    @Override
    public B getOneBackup(LambdaQueryWrapper<B> wrapper) {
        return this.backupMapper.selectOne(wrapper);
    }

    @Override
    public void backup(T entity, String operator, String backupRemark) {
        B backupEntity = entity.toBackupEntity();
        backupEntity.setBackupId(IdUtils.getSnowflakeId());
        backupEntity.setBackupBy(operator);
        backupEntity.setBackupTime(LocalDateTime.now());
        backupEntity.setBackupRemark(backupRemark);
        backupMapper.insert(backupEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIdAndBackup(T entity, String operator, String backupRemark) {
        this.removeById(entity);
        this.backup(entity, operator, backupRemark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAndBackup(Wrapper<T> wrapper, String operator, String backupRemark) {
        List<T> list = this.list(wrapper);
        for (T entity : list) {
            this.backup(entity, operator, backupRemark);
        }

        this.remove(wrapper);
    }

    @Override
    public void recover(Serializable backupEntityId) {
        B backupEntity = backupMapper.selectById(backupEntityId);
        Objects.requireNonNull(backupEntity);
        this.backupMapper.deleteById(backupEntity);

        T entity = backupEntity.toSourceEntity();
        this.save(entity);
    }

    @Override
    public void recover(B backupEntity) {
        Objects.requireNonNull(backupEntity);
        this.backupMapper.deleteById(backupEntity);

        T entity = backupEntity.toSourceEntity();
        this.save(entity);
    }

    @Override
    public void deleteBackupById(B backupEntity) {
        this.backupMapper.deleteById(backupEntity);
    }

    @Override
    public void deleteBackupById(Serializable backupId) {
        this.backupMapper.deleteById(backupId);
    }

    @Override
    public void deleteBackupByIds(Collection<Serializable> backupIds) {
        this.backupMapper.deleteByIds(backupIds);
    }

    @Override
    public void deleteBackups(Wrapper<B> wrapper) {
        this.backupMapper.delete(wrapper);
    }
}
