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
    public List<SysDictData> selectDictDatasByType(String dictType);

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
    public void deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 加载字典缓存数据
     */
    public void loadingDictCache();

    /**
     * 清空字典缓存数据
     */
    public void clearDictCache();

    /**
     * 重置字典缓存数据
     */
    public void resetDictCache();

    /**
     * 新增保存字典类型信息
     * 
     * @param dictType 字典类型信息
     * @return 结果
     */
    public void insertDictType(SysDictType dictType);

    /**
     * 修改保存字典类型信息
     * 
     * @param dictType 字典类型信息
     * @return 结果
     */
    public void updateDictType(SysDictType dictType);

    /**
     * 校验字典类型称是否唯一
     * 
     * @param dictType 字典类型
     * @return 结果
     */
    public boolean checkDictTypeUnique(SysDictType dictType);

    /**
     * 格式化列表指定字段字典数据
     * 
     * @param <T>
     * @param type
     * @param list
     * @param getter
     * @param setter
     */
	public <T> void decode(String type, List<T> list, Function<T, String> getter, BiConsumer<T, String> setter);

}
