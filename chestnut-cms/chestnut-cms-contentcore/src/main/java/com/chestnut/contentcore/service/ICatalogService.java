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
package com.chestnut.contentcore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface ICatalogService extends IService<CmsCatalog> {
	
	/**
	 * 根据栏目ID查找栏目数据
	 * 
	 * @param catalogId
	 * @return
	 */
	CmsCatalog getCatalog(Long catalogId);

	/**
	 * 根据栏目别名查找栏目数据
	 * 
	 * @param siteId
	 * @param catalogAlias
	 * @return
	 */
	CmsCatalog getCatalogByAlias(Long siteId, String catalogAlias);

	/**
	 * 构建栏目树数据
	 * 
	 * @param catalogs
	 * @return
	 */
	List<TreeNode<String>> buildCatalogTreeData(List<CmsCatalog> catalogs, BiConsumer<CmsCatalog, TreeNode<String>> consumer);

	/**
	 * 校验栏目别名、目录是否重复
	 *
	 * @param siteId
	 * @param catalogId
	 * @param alias
	 * @param path
	 * @return
	 */
	boolean checkCatalogUnique(Long siteId, Long catalogId, String alias, String path);

    /**
     * 添加栏目
     * 
     * @param dto
     * @throws IOException 
     */
	CmsCatalog addCatalog(CatalogAddDTO dto);

	/**
	 * 批量添加栏目
	 *
	 * @param dto
	 */
	void batchAddCatalog(CatalogBatchAddDTO dto);

	/**
	 * 编辑栏目
	 * 
	 * @param dto
	 * @return
	 * @throws IOException 
	 */
	CmsCatalog editCatalog(CatalogUpdateDTO dto) throws IOException;
	
	/**
	 * 删除栏目
	 * 
	 * @param catalogId
	 * @return
	 */
	CmsCatalog deleteCatalog(long catalogId, LoginUser operator);

	/**
	 * 获取栏目链接
	 * 
	 * @param catalog
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	String getCatalogLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview);

	String getCatalogListLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview);

	/**
	 * 栏目扩展配置应用到指定栏目或子栏目
	 * 
	 * @param dto
	 * @return
	 */
	void applyConfigPropsToChildren(CatalogApplyConfigPropsDTO dto);

	/**
	 * 栏目发布通道配置应用到子栏目
	 * 
	 * @param dto
	 * @return
	 */
	void applyPublishPipePropsToChildren(CatalogApplyPublishPipeDTO dto);
	
	/**
	 * 应用站点指定默认模板配置到指定的栏目
	 * 
	 * @param dto
	 * @return
	 */
	void applySiteDefaultTemplateToCatalog(SiteDefaultTemplateDTO dto);

	/**
	 * 清理栏目缓存
	 * 
	 * @param catalog
	 */
	void clearCache(CmsCatalog catalog);

	/**
	 * 改变栏目显示状态
	 * 
	 * @param catalogId
	 * @param visible
	 */
	void changeVisible(Long catalogId, String visible);

	/**
	 * 转移栏目
	 * 
	 * @param fromCatalog
	 * @param toCatalog
	 * @return 
	 */
	AsyncTask moveCatalog(CmsCatalog fromCatalog, CmsCatalog toCatalog);
	
	/**
	 * 配置静态化模板上下文中的静态化文件路径
	 * 
	 * @param catalog
	 * @param context
	 * @param hasIndex
	 */
	void setStaticPath(CmsCatalog catalog, TemplateContext context, boolean hasIndex);

	/**
	 * 保存栏目扩展配置
	 * 
	 * @param catalogId 栏目ID
	 * @param configs 配置信息
	 * @param operator 操作人
	 */
	void saveCatalogExtends(Long catalogId, Map<String, String> configs, String operator);
	
	/**
	 * 栏目排序
	 * 
	 * @param catalogId 栏目ID
	 * @param sort 排序值
	 */
	void sortCatalog(Long catalogId, Integer sort);

	/**
	 * 栏目内容数变更
	 *
	 * @param catalogId 栏目ID
	 * @param delta 变更数量
	 */
	void changeContentCount(Long catalogId, int delta);

	/**
	 * 清空栏目
	 */
	AsyncTask clearCatalog(ClearCatalogDTO dto);

	/**
	 * 合并栏目
	 */
    AsyncTask mergeCatalogs(MergeCatalogDTO dto);
}
