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
package com.chestnut.contentcore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IPublishPipeService extends IService<CmsPublishPipe> {

	/**
	 * 新增发布通道数据
	 * 
	 * @param publishPipe  发布通道信息
	 */
	void addPublishPipe(CmsPublishPipe publishPipe) throws IOException;

	/**
	 * 修改发布通道数据
	 * 
	 * @param publishPipe 发布通道信息
	 */
	void savePublishPipe(CmsPublishPipe publishPipe) throws IOException;
	
	/**
	 * 删除发布通道
	 * 
	 * @param publishPipeId 发布通道ID
	 */
	void deletePublishPipe(List<Long> publishPipeId);
	
	/**
	 * 删除指定站点发布通道
	 * 
	 * @param site 站点信息
	 */
	void deletePublishPipeBySite(CmsSite site);

	 /**
      * 获取指定站点可用发布通道
      *
	  * @param siteId 站点ID
	  * @return 结果列表
      */
    List<CmsPublishPipe> getPublishPipes(Long siteId);

    /**
     * 获取指定站点所有发布通道
     * 
     * @param siteId 站点ID
	 * @return 结果列表
     */
    List<CmsPublishPipe> getAllPublishPipes(Long siteId);

	/**
	 * 获取站点下指定使用场景的发布通道数据列表
	 * 
	 * @param siteId 站点ID
	 * @param useType 使用方式
	 * @param props 数据集合
	 * @return 结果列表
	 */
	List<PublishPipeProp> getPublishPipeProps(Long siteId, PublishPipePropUseType useType,
			Map<String, Map<String, Object>> props);

	/**
	 * 获取发布通道属性值
	 * 
	 * @param propKey 发布通道属性唯一标识
	 * @param publishPipeCode 发布通道编码
	 * @param props 数据集合
	 * @return 属性值
	 */
	String getPublishPipePropValue(String propKey, String publishPipeCode, Map<String, Map<String, Object>> props);
}
