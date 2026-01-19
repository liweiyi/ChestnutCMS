/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.db.mybatisplus.HasDAO;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.dao.CmsContentDAO;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.CopyContentDTO;
import com.chestnut.contentcore.domain.dto.MoveContentDTO;
import com.chestnut.contentcore.domain.dto.SetTopContentDTO;
import com.chestnut.contentcore.domain.dto.SortContentDTO;

import java.util.List;
import java.util.Map;

public interface IContentService extends HasDAO<CmsContentDAO> {

	/**
	 * 添加内容
	 */
	AsyncTask addContent(IContent<?> content);

	void addContent0(IContent<?> content);
	
	/**
	 * 更新内容
	 */
	AsyncTask saveContent(IContent<?> content);

	/**
	 * 删除内容
	 */
	void deleteContents(List<Long> contentIds, LoginUser operator);


	void deleteContent(CmsContent cmsContent, LoginUser operator, Map<String, Object> params);

	/**
	 * 恢复回收站删除的内容到指定栏目
	 */
	void recoverContents(List<Long> contentIds, LoginUser operator);
	
	/**
	 * 删除指定栏目内容
	 */
	void deleteContentsByCatalog(CmsCatalog catalog, boolean includeChild, LoginUser operator);

    String getContentStaticPath(CmsContent content, String publishPipeCode);

    /**
	 * 获取内容链接
	 * 
	 * @param content 内容
	 * @param publishPipeCode 发布通道编码
	 * @param isPreview 是否预览
	 */
	String getContentLink(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview);

	/**
	 * 锁定内容
	 * 
	 * @param contentId 内容ID
	 * @param operator 操作人
	 */
	void lock(Long contentId, String operator);

	/**
	 * 解锁内容
	 *
	 * @param contentId 内容ID
	 * @param operator 操作人
	 */
	void unLock(Long contentId, String operator);

	/**
	 * 复制内容
	 */
	AsyncTask copy(CopyContentDTO dto);

	/**
	 * 转移内容
	 */
	AsyncTask move(MoveContentDTO dto);

	void moveContent(CmsContent cmsContent, CmsCatalog toCatalog, LoginUser operator);

	/**
	 * 校验重复标题，存在重复标题返回true
	 *
	 * @param siteId 站点ID
	 * @param catalogId 栏目ID
	 * @param contentId 内容ID
	 * @param title 标题
	 * @return 结构
	 */
	boolean checkSameTitle(Long siteId, Long catalogId, Long contentId, String title);

    /**
	 * 置顶
	 */
	void setTop(SetTopContentDTO dto);

	/**
	 * 取消置顶
	 */
	void cancelTop(List<Long> contentIds, LoginUser operator);

	/**
	 * 下线内容
	 *
	 * @return
	 */
	AsyncTask offline(List<Long> contentIds, LoginUser operator);

	/**
	 * 下线内容
	 */
    void offline(CmsContent cmsContent, LoginUser operator);

    /**
	 * 排序，将指定内容排到目标内容之前
	 */
	void sort(SortContentDTO dto);

	void toPublish(CmsContent cmsContent, LoginUser operator);

	/**
	 * 归档
	 */
	void archive(List<Long> contentIds, LoginUser operator);

	/**
	 * 删除指定内容静态文件
	 */
	void deleteStaticFiles(CmsContent contentEntity);

	/**
	 * 删除备份表数据
	 */
	void deleteRecycleContents(List<Long> backupIds);

	/**
	 * 待发布指定内容
	 */
    void toPublish(List<Long> contentIds, LoginUser loginUser);
}
