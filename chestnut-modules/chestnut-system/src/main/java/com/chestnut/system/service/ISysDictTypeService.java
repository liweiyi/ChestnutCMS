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
package com.chestnut.system.service;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.domain.SysDictType;

/**
 * 字典 业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysDictTypeService extends IService<SysDictType> {

    /**
     * 根据字典类型查询字典数据
     * 
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDatasByType(String dictType);

    /**
     * 根据字典类型和数据值获取字典项
     * 
     * @param dictType
     * @param dictValue
     * @return
     */
	Optional<SysDictData> optDictData(String dictType, String dictValue);

    /**
     * 批量删除字典信息
     * 
     * @param dictIds 需要删除的字典ID
     */
    void deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 加载字典缓存数据
     */
    void loadingDictCache();

    /**
     * 清空字典缓存数据
     */
    void clearDictCache();

    /**
     * 重置字典缓存数据
     */
    void resetDictCache();

    /**
     * 新增保存字典类型信息
     * 
     * @param dictType 字典类型信息
     * @return 结果
     */
    void insertDictType(SysDictType dictType);

    /**
     * 修改保存字典类型信息
     * 
     * @param dictType 字典类型信息
     * @return 结果
     */
    void updateDictType(SysDictType dictType);

    /**
     * 校验字典类型称是否唯一
     * 
     * @param dictType 字典类型
     * @return 结果
     */
    boolean checkDictTypeUnique(SysDictType dictType);

    /**
     * 格式化列表指定字段字典数据
     * 
     * @param <T>
     * @param type
     * @param list
     * @param getter
     * @param setter
     */
	<T> void decode(String type, List<T> list, Function<T, String> getter, BiConsumer<T, String> setter);

}
