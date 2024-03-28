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
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.CmsTemplate;
import com.chestnut.contentcore.domain.dto.TemplateAddDTO;
import com.chestnut.contentcore.domain.dto.TemplateUpdateDTO;
import com.chestnut.contentcore.template.ITemplateType;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ITemplateService extends IService<CmsTemplate> {

	/**
	 * 清理站点指定模板静态化缓存
	 *
	 * @param templateKey 唯一标识：相对resourceRoot路径
	 */
	void clearTemplateStaticContentCache(String templateKey);

	/**
	 * 清理站点所有模板静态化缓存
	 *
	 * @param site 站点信息
	 */
    void clearSiteAllTemplateStaticContentCache(CmsSite site);

    /**
	 * 获取模板类型
	 * 
	 * @param typeid 模板类型唯一标识
	 */
	ITemplateType getTemplateType(String typeid);

	/**
	 * 获取模板静态化内容缓存，主要区块模板使用
	 * 
	 * @param templateKey 相对resourceRoot路径
	 */
	String getTemplateStaticContentCache(String templateKey);

	/**
	 * 缓存模板静态化内容
	 * 
	 * @param templateKey 相对resourceRoot路径
	 */
	void setTemplateStaticContentCache(String templateKey, String staticContent);

	/**
	 * 扫描模板目录，创建模板数据库记录
	 * 
	 * @param site 站点数据
	 */
	void scanTemplates(CmsSite site);

	/**
	 * 保存模板内容
	 */
	void saveTemplate(CmsTemplate template, TemplateUpdateDTO dto) throws IOException;

	/**
	 * 模板文件重命名
	 *
	 * @param template 模板信息
	 * @param path 路径
	 * @param remark 备注
	 * @param operator 操作人
	 */
	void renameTemplate(CmsTemplate template, String path, String remark, String operator) throws IOException;

	/**
	 * 获取模板文件
	 * 
	 * @param template 模板信息
	 */
	File getTemplateFile(CmsTemplate template);

	/**
	 * 查找模板文件
	 * 
	 * @param site 站点信息
	 * @param templatePath 模板路径
	 * @param publishPipeCode 发布通道编码
	 */
	File findTemplateFile(CmsSite site, String templatePath, String publishPipeCode);

	/**
	 * 新建模板文件
	 * 
	 * @param dto 模板数据
	 */
	void addTemplate(TemplateAddDTO dto) throws IOException;

	/**
	 * 删除模板
	 * @param templateIds 模板ID列表
	 */
	void deleteTemplates(CmsSite site, List<Long> templateIds) throws IOException;
}
