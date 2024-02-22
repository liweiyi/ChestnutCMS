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
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IResourceService extends IService<CmsResource> {

	/**
	 * 上传资源
	 * 
	 * @param dto
	 * @return 
	 * @throws IOException
	 */
	CmsResource addResource(ResourceUploadDTO dto) throws IOException;

	/**
	 * 修改资源
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	CmsResource editResource(ResourceUploadDTO dto) throws IOException;

	/**
	 * 添加base64图片资源
	 * 
	 * @param site
	 * @param operator
	 * @param base64Data
	 * @throws IOException
	 */
	CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException;

	/**
	 * 添加本地文件到资源库
	 *
	 * @param site
	 * @param operator
	 * @param imageFile
	 * @return
	 * @throws IOException
	 */
	CmsResource addImageFromFile(CmsSite site, String operator, File imageFile) throws IOException;

	/**
	 * 删除资源
	 * 
	 * @param resourceIds
	 * @return
	 */
	void deleteResource(List<Long> resourceIds);

	/**
	 * 获取资源访问路径
	 * 
	 * @param resource
	 * @param preview
	 * @return
	 */
	String getResourceLink(CmsResource resource, boolean preview);

	/**
	 * 下载资源文件
	 * 
	 * @param resource
	 * @param os
	 */
	void downloadResource(CmsResource resource, HttpServletResponse os);

	/**
	 * 下载远程图片
	 * 
	 * @param url
	 * @param siteId
	 * @param operator
	 * @return
	 * @throws IOException
	 * @throws Exception 
	 */
	CmsResource downloadImageFromUrl(String url, long siteId, String operator) throws Exception;
}
