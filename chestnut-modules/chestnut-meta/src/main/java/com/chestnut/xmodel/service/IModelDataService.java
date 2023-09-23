package com.chestnut.xmodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.common.db.util.SqlBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IModelDataService {

	/**
	 * 根据主键获取模型数据
	 *
	 * @param modelId
	 * @param pkValues
	 * @return
	 */
	Map<String, Object> getModelDataByPkValue(Long modelId, Map<String, Object> pkValues);

	/**
	 * 保存模型数据
	 * 
	 * @param modelId
	 * @param params
	 * @return
	 */
	void saveModelData(Long modelId, Map<String, Object> params);

	/**
	 * 添加元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
    void addModelData(Long modelId, Map<String, Object> data);

	/**
	 * 更新元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
	void updateModelData(Long modelId, Map<String, Object> data);

	/**
	 * 删除元数据模型数据
	 *
	 * @param modeId
	 * @param pkValues
	 */
	void deleteModelDataByPkValue(Long modeId, List<Map<String, String>> pkValues);

    List<Map<String, Object>> selectModelDataList(Long modelId, Consumer<SqlBuilder> consumer);

	IPage<Map<String, Object>> selectModelDataPage(Long modelId, IPage<Map<String, Object>> page,
												   Consumer<SqlBuilder> consumer);
}
