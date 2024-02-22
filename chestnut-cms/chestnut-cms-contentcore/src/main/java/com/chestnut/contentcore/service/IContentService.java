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
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.CopyContentDTO;
import com.chestnut.contentcore.domain.dto.MoveContentDTO;
import com.chestnut.contentcore.domain.dto.SetTopContentDTO;
import com.chestnut.contentcore.domain.dto.SortContentDTO;
import com.chestnut.contentcore.mapper.CmsContentMapper;

import java.io.IOException;
import java.util.List;

public interface IContentService extends IService<CmsContent> {

	/**
	 * 添加内容
	 * 
	 * @param content
	 * @return
	 */
	AsyncTask addContent(IContent<?> content);


	void addContent0(IContent<?> content);
	
	/**
	 * 更新内容
	 * 
	 * @param content
	 * @return
	 */
	AsyncTask saveContent(IContent<?> content);

	/**
	 * 删除内容
	 * 
	 * @param contentIds
	 * @return
	 */
	void deleteContents(List<Long> contentIds, LoginUser operator);


	/**
	 * 恢复回收站删除的内容到指定栏目
	 * 
	 * @param contentIds
	 * @param operator
	 */
	void recoverContents(List<Long> contentIds, LoginUser operator);
	
	/**
	 * 删除指定栏目内容
	 * 
	 * @param catalog
	 * @return
	 */
	boolean deleteContentsByCatalog(CmsCatalog catalog, LoginUser operator);

	/**
	 * 获取内容链接
	 * 
	 * @param content
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	String getContentLink(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview);

	/**
	 * 锁定内容
	 * 
	 * @param contentId
	 * @param operator
	 * @return
	 */
	void lock(Long contentId, String operator);

	/**
	 * 解锁内容
	 * 
	 * @param contentId
	 * @param operator
	 * @return
	 */
	void unLock(Long contentId, String operator);

	/**
	 * 复制内容
	 * 
	 * @param dto
	 * @return
	 */
	void copy(CopyContentDTO dto);

	/**
	 * 转移内容
	 * 
	 * @param dto
	 * @return
	 */
	void move(MoveContentDTO dto);

	/**
	 * 校验重复标题，存在重复标题返回true
	 *
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @param title
	 * @return
	 */
	boolean checkSameTitle(Long siteId, Long catalogId, Long contentId, String title);

	/**
	 * 置顶
	 * 
	 * @param dto
	 * @return
	 */
	void setTop(SetTopContentDTO dto);

	/**
	 * 取消置顶
	 * 
	 * @param contentIds
	 * @return
	 */
	void cancelTop(List<Long> contentIds, LoginUser operator);

	/**
	 * 下线内容
	 * 
	 * @param contentIds
	 * @return
	 */
	void offline(List<Long> contentIds, LoginUser operator);

	/**
	 * 排序，将指定内容排到目标内容之前
	 * 
	 * @param dto
	 * @return
	 */
	void sort(SortContentDTO dto);

	/**
	 * 归档
	 * 
	 * @param contentIds
	 * @return
	 */
	void archive(List<Long> contentIds, LoginUser operator);

	/**
	 * 删除指定内容静态文件
	 * 
	 * @param contentEntity
	 * @throws IOException 
	 */
	void deleteStaticFiles(CmsContent contentEntity);

	/**
	 * 删除备份表数据
	 * 
	 * @param backupIds
	 */
	void deleteRecycleContents(List<Long> backupIds);

    CmsContentMapper getContentMapper();

	/**
	 * 待发布指定内容
	 *
	 * @param contentIds
	 * @param loginUser
	 */
    void toPublish(List<Long> contentIds, LoginUser loginUser);
}
