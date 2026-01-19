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

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface IResourceService extends IService<CmsResource> {

	/**
	 * 上传资源
	 */
	CmsResource addResource(ResourceUploadDTO dto) throws IOException;

	/**
	 * 修改资源
	 */
	CmsResource editResource(ResourceUploadDTO dto) throws IOException;

	/**
	 * 添加base64图片资源
	 * 
	 * @param site 所属站点
	 * @param operator 操作人
	 * @param base64Data BASE64图片信息
	 */
	CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException;

	/**
	 * 添加本地文件到资源库
	 *
	 * @param site 所属站点
	 * @param operator 操作人
	 * @param imageFile 图片文件
	 */
	CmsResource addImageFromFile(CmsSite site, String operator, File imageFile) throws IOException;

	void processResource(CmsResource resource, IResourceType resourceType, CmsSite site, byte[] bytes) throws IOException;

	/**
	 * 删除资源
	 * 
	 * @param resourceIds 素材ID列表
	 */
	void deleteResource(List<Long> resourceIds);

	/**
	 * 获取资源访问路径
	 * 
	 * @param resource 素材信息
	 * @param publishPipeCode 发布通道编码
	 * @param preview 是否预览模式
	 */
	String getResourceLink(CmsResource resource, String publishPipeCode, boolean preview);

	/**
	 * 下载资源文件
	 */
	void downloadResource(CmsResource resource, HttpServletResponse os);

	/**
	 * 下载远程图片
	 * 
	 * @param url 远程图片地址
	 * @param siteId 站点ID
	 * @param operator 操作人
	 */
	CmsResource downloadImageFromUrl(String url, long siteId, String operator) throws Exception;

	/**
	 * 解析html中的图片标签，如果是远程地址图片则下载到资源库中并将图片标签的src替换为资源内部链接
	 *
	 * @param html HTML文本
	 * @param site 所属站点
	 * @param operator 操作人
	 */
    String downloadRemoteImages(String html, CmsSite site, String operator);

	/**
	 * 检查图片资源缩略图，如果不存在则生成
	 *
	 * @param internalURL 内部链接
	 * @param width 缩略图宽度
	 * @param height 缩略图高度
	 */
	boolean createThumbnailIfNotExists(InternalURL internalURL, int width, int height) throws Exception;

	/**
	 * 处理默认缩略图
	 *
	 * @param site 站点
	 * @param images 源图列表
	 * @param thumbnailsConsumer 缩略图消费者
	 */
	void dealDefaultThumbnail(CmsSite site, List<String> images, Consumer<List<String>> thumbnailsConsumer);

	/**
	 * 处理内容引导图缩略图
	 *
	 * @param site 站点
	 * @param imageSrc 源图列表
	 * @param thumbnailConsumer 缩略图消费者
	 */
	void dealDefaultThumbnail(CmsSite site, String imageSrc, Consumer<String> thumbnailConsumer);

    /**
     * 读取资源流
     *
     * @param resource 资源
     */
    InputStream readResource(CmsResource resource);
}
