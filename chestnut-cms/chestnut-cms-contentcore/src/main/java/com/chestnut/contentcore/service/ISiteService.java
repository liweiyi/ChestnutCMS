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
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.SiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDefaultTemplateDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface ISiteService extends IService<CmsSite> {

	/**
	 * 校验站点名称及目录是否唯一
	 *
	 * @param siteName
	 * @param sitePath
	 * @param siteId
	 * @return
	 */
	boolean checkSiteUnique(String siteName, String sitePath, Long siteId);

    /**
     * 获取当前站点，保存在token中
     */
    CmsSite getCurrentSite(HttpServletRequest request);

	/**
	 * 获取站点数据
	 * 
	 * @param siteId
	 * @return
	 */
	CmsSite getSite(Long siteId);

	/**
	 * 新增站点数据
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	CmsSite addSite(SiteDTO dto) throws IOException;
	
	/**
	 * 修改站点数据
	 * 
	 * @param site
	 * @return
	 * @throws IOException
	 */
	CmsSite saveSite(SiteDTO site) throws IOException;
	
	/**
	 * 删除站点数据
	 * 
	 * @param siteId
	 * @return
	 * @throws IOException
	 */
	void deleteSite(Long siteId) throws IOException;

	/**
	 * 保存站点默认模板配置
	 * 
	 * @param dto
	 * @return
	 */
	void saveSiteDefaultTemplate(SiteDefaultTemplateDTO dto);

	/**
	 * 清理站点缓存数据
	 * 
	 * @param siteId
	 */
	void clearCache(long siteId);

	/**
	 * 保存站点扩展配置
	 * 
	 * @param site
	 * @param configs
	 * @param operator
	 */
	void saveSiteExtend(CmsSite site, Map<String, String> configs, String operator);
}
