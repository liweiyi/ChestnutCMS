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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.storage.StorageReadArgs;
import com.chestnut.common.storage.StorageReadArgs.StorageReadArgsBuilder;
import com.chestnut.common.storage.StorageWriteArgs;
import com.chestnut.common.storage.StorageWriteArgs.StorageWriteArgsBuilder;
import com.chestnut.common.storage.exception.StorageErrorCode;
import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.*;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.core.impl.ResourceType_Image;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.mapper.CmsResourceMapper;
import com.chestnut.contentcore.properties.FileStorageArgsProperty;
import com.chestnut.contentcore.properties.FileStorageArgsProperty.FileStorageArgs;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.ResourceUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<CmsResourceMapper, CmsResource> implements IResourceService {

	private final Map<String, IFileStorageType> fileStorageTypes;

	private final ISiteService siteService;

	@Override
	public CmsResource downloadImageFromUrl(String url, long siteId, String operator) throws Exception {
		if (!ServletUtils.isHttpUrl(url)) {
			throw CommonErrorCode.INVALID_REQUEST_ARG.exception("url");
		}

		String suffix = FileExUtils.getImageSuffix(url);
		IResourceType resourceType = ContentCoreUtils.getResourceType(ResourceType_Image.ID);
		if (!resourceType.check(suffix)) {
			throw ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix);  // 不支持的图片格式
		}
		CmsSite site = siteService.getSite(siteId);

		long resourceId = IdUtils.getSnowflakeId();
		String fileName = resourceId + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		// 下载图片
		byte[] imageBytes = HttpUtils.syncDownload(url);
		Assert.notNull(imageBytes, () -> CommonErrorCode.REQUEST_FAILED.exception(url));

		CmsResource resource = new CmsResource();
		resource.setResourceId(resourceId);
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(fileName);
		resource.setName(fileName);
		resource.setSourceUrl(url);
		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.setSuffix(suffix);
		resource.createBy(operator);
		// 处理资源
		this.processResource(resource, resourceType, site, imageBytes);
		this.save(resource);
		return resource;
	}

	@Override
	public CmsResource addResource(ResourceUploadDTO dto)
			throws IOException {
		String suffix = FileExUtils.getExtension(Objects.requireNonNull(dto.getFile().getOriginalFilename()));
		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(dto.getSite().getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(dto.getFile().getOriginalFilename());
		resource.setName(StringUtils.isEmpty(dto.getName()) ? dto.getFile().getOriginalFilename() : dto.getName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(dto.getSite());
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);

		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(dto.getOperator().getUsername());
		resource.setRemark(dto.getRemark());
		// 处理资源
		this.processResource(resource, resourceType, dto.getSite(), dto.getFile().getBytes());
		this.save(resource);
		return resource;
	}

	@Override
	public CmsResource editResource(ResourceUploadDTO dto)
			throws IOException {
		String suffix = FileExUtils.getExtension(Objects.requireNonNull(dto.getFile().getOriginalFilename()));
		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = this.getById(dto.getResourceId());
		Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", dto.getResourceId()));

		resource.setResourceType(resourceType.getId());
		resource.setFileName(dto.getFile().getOriginalFilename());
		resource.setName(StringUtils.isEmpty(dto.getName()) ? dto.getFile().getOriginalFilename() : dto.getName());
		resource.setSuffix(suffix);

		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String path = StringUtils.substringBeforeLast(resource.getPath(), "/") + fileName;

		resource.setPath(path);
		resource.updateBy(dto.getOperator().getUsername());
		resource.setRemark(dto.getRemark());
		// 处理资源
		this.processResource(resource, resourceType, dto.getSite(), dto.getFile().getBytes());
		this.updateById(resource);
		return resource;
	}

	@Override
	public CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException {
		if (!base64Data.startsWith("data:image/")) {
			return null;
		}
		String suffix = base64Data.substring(11, base64Data.indexOf(";"));

		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName("base64_" + resource.getResourceId());
		resource.setName(resource.getFileName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);

		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(operator);

		String base64Str = StringUtils.substringAfter(base64Data, ",");
		byte[] imageBytes = Base64.getDecoder().decode(base64Str);
		this.processResource(resource, resourceType, site, imageBytes);
		this.save(resource);
		return resource;
	}

	@Override
	public CmsResource addImageFromFile(CmsSite site, String operator, File imageFile) throws IOException {
		if (Objects.isNull(imageFile) || !imageFile.exists()) {
			return null;
		}
		String suffix = FileExUtils.getExtension(imageFile.getName());
		IResourceType resourceType = ResourceUtils.getResourceTypeBySuffix(suffix);
		Assert.notNull(resourceType, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(suffix));

		CmsResource resource = new CmsResource();
		resource.setResourceId(IdUtils.getSnowflakeId());
		resource.setSiteId(site.getSiteId());
		resource.setResourceType(resourceType.getId());
		resource.setFileName(resource.getResourceId().toString() + "." + suffix);
		resource.setName(resource.getFileName());
		resource.setSuffix(suffix);

		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String dir = resourceType.getUploadPath()
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + StringUtils.SLASH;
		FileExUtils.mkdirs(siteResourceRoot + dir);

		resource.setPath(dir + fileName);
		resource.setStatus(EnableOrDisable.ENABLE);
		resource.createBy(operator);

		byte[] bytes = FileUtils.readFileToByteArray(imageFile);
		this.processResource(resource, resourceType, site, bytes);
		this.save(resource);
		return resource;
	}

	private void processResource(CmsResource resource, IResourceType resourceType, CmsSite site, byte[] bytes) throws IOException {
		// 处理资源，图片属性读取、水印等
		bytes = resourceType.process(resource, bytes);
		// 写入磁盘/OSS
		String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
		IFileStorageType fst = this.getFileStorageType(fileStorageType);
		FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
		// 写入参数设置
		StorageWriteArgsBuilder builder = StorageWriteArgs.builder();
		builder.bucket(fileStorageArgs.getBucket());
		if (LocalFileStorageType.TYPE.equals(fst.getType())) {
			builder.bucket(SiteUtils.getSiteResourceRoot(site));
		} else {
			builder.accessKey(fileStorageArgs.getAccessKey());
			builder.accessSecret(fileStorageArgs.getAccessSecret());
			builder.endpoint(fileStorageArgs.getEndpoint());
		}
		builder.path(resource.getPath());
		builder.inputStream(new ByteArrayInputStream(bytes));
		fst.write(builder.build());
		resource.setStorageType(fst.getType());
		// 内部链接
		resource.setInternalUrl(InternalDataType_Resource.getInternalUrl(resource));
	}

	@Override
	public void deleteResource(List<Long> resourceIds) {
		List<CmsResource> resources = this.listByIds(resourceIds);
		if (!resources.isEmpty()) {
			CmsSite site = siteService.getSite(resources.get(0).getSiteId());
			String siteRoot = SiteUtils.getSiteResourceRoot(site);
			resources.forEach(r -> {
				// 删除资源文件
				try {
					File file = new File(siteRoot + r.getPath());
					FileUtils.delete(file);
					final String fileNamePrefix = StringUtils.substringBeforeLast(file.getName(), ".");
					// 删除图片缩略图
					File[] others = file.getParentFile().listFiles(
							(dir, name) -> name.startsWith(fileNamePrefix)
					);
					if (Objects.nonNull(others)) {
						for (File f : others) {
							FileUtils.delete(f);
						}
					}
				} catch (IOException e) {
					log.error("Delete resource file failed: " + r.getPath());
				}
				// 删除数据库记录
				this.removeById(r.getResourceId());
			});
		}
	}

	@Override
	public String getResourceLink(CmsResource resource, String publishPipeCode, boolean isPreview) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		String prefix = ResourceUtils.getResourcePrefix(resource.getStorageType(), site, publishPipeCode, isPreview);
		return prefix + resource.getPath();
	}

	@Override
	public void downloadResource(CmsResource resource, HttpServletResponse response) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		IFileStorageType storagetType = this.getFileStorageType(resource.getStorageType());
		StorageReadArgsBuilder builder = StorageReadArgs.builder();
		FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
		if (fileStorageArgs != null) {
			builder.endpoint(fileStorageArgs.getEndpoint())
					.accessKey(fileStorageArgs.getAccessKey())
					.accessSecret(fileStorageArgs.getAccessSecret())
					.bucket(fileStorageArgs.getBucket())
					.path(resource.getPath());
		}
		InputStream is = storagetType.read(builder.build());
		try {
			IOUtils.copy(is, response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IFileStorageType getFileStorageType(String type) {
		IFileStorageType fileStorageType = fileStorageTypes.get(IFileStorageType.BEAN_NAME_PREIFX + type);
		Assert.notNull(fileStorageType, () -> StorageErrorCode.UNSUPPORTED_STORAGE_TYPE.exception(type));
		return fileStorageType;
	}

	/**
	 * 解析html中的图片标签，如果是远程地址图片则下载到资源库中并将图片标签的src替换为资源内部链接
	 *
	 * @param html HTML文本
	 * @param site 所属站点
	 * @param operator 操作人
	 */
	@Override
	public String downloadRemoteImages(String html, CmsSite site, String operator) {
		if (StringUtils.isBlank(html)) {
			return html;
		}
		Matcher matcher = ResourceUtils.ImgHtmlTagPattern.matcher(html);
		int lastEndIndex = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find(lastEndIndex)) {
			int s = matcher.start();
			sb.append(html, lastEndIndex, s);

			String imgTag = matcher.group();
			String src = matcher.group(1);
			try {
				if (StringUtils.startsWithIgnoreCase(src, "data:image/")) {
					// base64图片保存到资源库
					CmsResource resource = addBase64Image(site, operator, src);
					if (Objects.nonNull(resource)) {
						imgTag = imgTag.replaceFirst("data:image/([^'\"]+)", src);
					}
				} else if (!InternalUrlUtils.isInternalUrl(src) && ServletUtils.isHttpUrl(src)) {
					// 非iurl的http链接则下载图片
					CmsResource resource = downloadImageFromUrl(src, site.getSiteId(), operator);
					if (Objects.nonNull(resource)) {
						imgTag = StringUtils.replaceEx(imgTag, src, resource.getInternalUrl());
					}
				}
			} catch (Exception e1) {
				String imgSrc = (src.startsWith("data:image/") ? src.substring(0, 20) : src);
				log.warn("Save image failed: " + imgSrc);
				AsyncTaskManager.addErrMessage("Download remote image failed: " + imgSrc);
			}
			sb.append(imgTag);
			lastEndIndex = matcher.end();
		}
		sb.append(html.substring(lastEndIndex));
		return sb.toString();
	}

	private final List<IResourceStat> resourceStats;

	/**
	 * TODO 统计资源引用
	 */
	public void statResourceUsage() {
		Map<Long, Integer> resourceIds = new HashMap<>();
		this.resourceStats.forEach(rs -> {
			Map<Long, Integer> quotedResource = rs.findQuotedResource();
		});
		// 站点Logo，栏目Logo，内容Logo

		// 页面部件区块Logo

		// 页面部件广告图

		// 文章内容

		// 图集内容

		// 音频内容

		// 视频内容
	}
}
