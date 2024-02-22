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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;

public interface IPublishPipeService extends IService<CmsPublishPipe> {

	/**
	 * 新增发布通道数据
	 * 
	 * @param publishPipe
	 * @throws IOException
	 */
	public void addPublishPipe(CmsPublishPipe publishPipe) throws IOException;

	/**
	 * 修改发布通道数据
	 * 
	 * @param publishPipe
	 * @throws IOException
	 */
	public void savePublishPipe(CmsPublishPipe publishPipe) throws IOException;
	
	/**
	 * 删除发布通道
	 * 
	 * @param publishPipeId
	 * @throws IOException
	 */
	public void deletePublishPipe(List<Long> publishPipeId);
	
	/**
	 * 删除指定站点发布通道
	 * 
	 * @param site
	 * @throws IOException
	 */
	public void deletePublishPipeBySite(CmsSite site);

	 /**
     * 获取指定站点可用发布通道
     * 
     * @param siteId
     */
    public List<CmsPublishPipe> getPublishPipes(Long siteId);

    /**
     * 获取指定站点所有发布通道
     * 
     * @param siteId
     */
    public List<CmsPublishPipe> getAllPublishPipes(Long siteId);

	/**
	 * 获取站点下指定使用场景的发布通道数据列表
	 * 
	 * @param siteId
	 * @param useType
	 * @param props
	 * @return
	 */
	List<PublishPipeProp> getPublishPipeProps(Long siteId, PublishPipePropUseType useType,
			Map<String, Map<String, Object>> props);

	/**
	 * 获取发布通道属性值
	 * 
	 * @param propKey
	 * @param publishPipeCode
	 * @param props
	 * @return
	 */
	String getPublishPipePropValue(String propKey, String publishPipeCode, Map<String, Map<String, Object>> props);
}
