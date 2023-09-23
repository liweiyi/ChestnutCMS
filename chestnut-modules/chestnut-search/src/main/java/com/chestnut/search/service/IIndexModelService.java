package com.chestnut.search.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.search.domain.IndexModel;
import com.chestnut.search.domain.dto.SearchModelDTO;

public interface IIndexModelService extends IService<IndexModel> {
	
	/**
	 * 根据模型编码获取包含模型字段的模型实例缓存
	 * 
	 * @param modelCode
	 * @return
	 */
	SearchModelDTO getIndexModel(String modelCode);

	/**
	 * 添加索引模型
	 * 
	 * @param model
	 */
	void addIndexModel(SearchModelDTO dto);
	
	/**
	 * 删除索引模型
	 * 
	 * @param modelIds
	 */
	void deleteIndexModel(List<Long> modelIds);
}