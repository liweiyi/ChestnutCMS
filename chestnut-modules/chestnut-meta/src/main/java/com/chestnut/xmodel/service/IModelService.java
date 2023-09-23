package com.chestnut.xmodel.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.XModelDTO;

public interface IModelService extends IService<XModel> {

	/**
	 * 添加扩展模型
	 * 
	 * @param dto
	 * @return
	 */
	void addModel(XModelDTO dto);
	
	/**
	 * 编辑扩展模型
	 * 
	 * @param dto
	 * @return
	 */
	void editModel(XModelDTO dto);
	
	/**
	 * 删除扩展模型
	 * 
	 * @param modelIds
	 * @return
	 */
	void deleteModel(List<Long> modelIds);

	/**
	 * 模型数据自定义表
	 */
	List<String> listModelDataTables(String type);

	/**
	 * 模型数据表字段
	 */
	List<String> listModelTableFields(XModel model);

	MetaModel getMetaModel(Long modelId);

	void clearMetaModelCache(Long modelId);
}
