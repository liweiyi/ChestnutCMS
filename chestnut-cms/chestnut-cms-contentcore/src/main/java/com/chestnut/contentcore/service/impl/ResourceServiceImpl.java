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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.storage.FileStorageService;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.utils.*;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.common.utils.image.ImageHelper;
import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.core.impl.ResourceType_Image;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.mapper.CmsResourceMapper;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import com.chestnut.contentcore.properties.ThumbnailHeightProperty;
import com.chestnut.contentcore.properties.ThumbnailWidthProperty;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<CmsResourceMapper, CmsResource> implements IResourceService {

	private final FileStorageService fileStorageService;

	private final List<IResourceStat> resourceStats;

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
		byte[] imageBytes = HttpUtils.syncDownload(url, true);
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

		String oldResourceType = resource.getResourceType();
		String oldPath = resource.getPath();

		resource.setResourceType(resourceType.getId());
		resource.setFileName(dto.getFile().getOriginalFilename());
		resource.setName(StringUtils.isEmpty(dto.getName()) ? dto.getFile().getOriginalFilename() : dto.getName());
		resource.setSuffix(suffix);
		String fileName = resource.getResourceId() + StringUtils.DOT + suffix;
		String path = StringUtils.substringBeforeLast(resource.getPath(), "/") + "/" + fileName;
		resource.setPath(path);
		resource.updateBy(dto.getOperator().getUsername());
		resource.setRemark(dto.getRemark());
		// 删除原文件
		if (!resource.getPath().equals(oldPath)) {
			String fileStorageType = FileStorageTypeProperty.getValue(dto.getSite().getConfigProps());
			IFileStorageType fst = fileStorageService.getFileStorageType(fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, dto.getSite());
			deleteResource(oldPath, oldResourceType, fileStorageHelper);
		}
		// 处理资源
		this.processResource(resource, resourceType, dto.getSite(), dto.getFile().getBytes());
		this.updateById(resource);
		return resource;
	}

	private void deleteResource(String path, String resourceType, FileStorageHelper fileStorageHelper) {
		fileStorageHelper.remove(path);
		// 删除图片缩略图
		if (ResourceType_Image.ID.equals(resourceType)) {
			String fileName = StringUtils.substringAfterLast(path, "/");
			final String fileNamePrefix = StringUtils.substringBeforeLast(fileName, ".") + "_";
			path = StringUtils.substringBeforeLast(path, "/") + "/" + fileNamePrefix;
			fileStorageHelper.removeByPrefix(path);
		}
	}

	@Override
	public CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException {
		if (!ImageUtils.isBase64Image(base64Data)) {
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

	@Override
	public void processResource(CmsResource resource, IResourceType resourceType, CmsSite site, byte[] bytes) throws IOException {
		resource.setWidth(0);
		resource.setHeight(0);
		resource.setFileSize((long) bytes.length);
		// 保存临时文件处理
		String tempDirectory = ResourceUtils.getResourceTempDirectory(site);
		File tempFile = new File(tempDirectory + resource.getPath());
		FileUtils.writeByteArrayToFile(tempFile, bytes);
		List<File> processed = resourceType.process(resource, tempFile);
		try {
			// 读取存储配置
			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
			IFileStorageType fst = fileStorageService.getFileStorageType(fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
			// 写入磁盘/OSS
			fileStorageHelper.write(resource.getPath(), tempFile);
			if (!processed.isEmpty()) {
				String prefix = StringUtils.substringBeforeLast(resource.getPath(), "/") + "/";
				for (File f : processed) {
					fileStorageHelper.write(prefix + f.getName(), f);
				}
			}
			// 设置资源参数
			resource.setStorageType(fileStorageType);
			resource.setInternalUrl(InternalDataType_Resource.getInternalUrl(resource));
			// 异步后续处理
			resourceType.asyncProcess(resource);
		} finally {
			// 删除临时文件
			FileUtils.delete(tempFile);
			for (File file : processed) {
				FileUtils.delete(file);
			}
		}
	}

	@Override
	public void deleteResource(List<Long> resourceIds) {
		List<CmsResource> resources = this.listByIds(resourceIds);
		if (!resources.isEmpty()) {
			CmsSite site = siteService.getSite(resources.get(0).getSiteId());
			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
			IFileStorageType fst = fileStorageService.getFileStorageType(fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
			resources.forEach(r -> {
				// 删除文件
				deleteResource(r.getPath(), r.getResourceType(), fileStorageHelper);
                // 删除数据库记录
				this.removeById(r.getResourceId());
			});
		}
	}

	@Override
	public String getResourceLink(CmsResource resource, String publishPipeCode, boolean isPreview) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		String storageType = FileStorageTypeProperty.getValue(site.getConfigProps());
		String prefix = ResourceUtils.getResourcePrefix(storageType, site, publishPipeCode, isPreview);
		return prefix + resource.getPath();
	}

	@Override
	public void downloadResource(CmsResource resource, HttpServletResponse response) {
		CmsSite site = this.siteService.getSite(resource.getSiteId());
		String storageTypeId = FileStorageTypeProperty.getValue(site.getConfigProps());
		IFileStorageType storageType = fileStorageService.getFileStorageType(storageTypeId);
		FileStorageHelper fileStorageHelper = FileStorageHelper.of(storageType, site);
		try (InputStream is = fileStorageHelper.read(resource.getPath())) {
			IOUtils.copy(is, response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
				if (ImageUtils.isBase64Image(src)) {
					// base64图片保存到资源库
					CmsResource resource = addBase64Image(site, operator, src);
					if (Objects.nonNull(resource)) {
						imgTag = StringUtils.replaceEx(imgTag, src, resource.getInternalUrl());
					}
				} else if (!InternalUrlUtils.isInternalUrl(src) && ServletUtils.isHttpUrl(src)) {
					// 非iurl的http链接则下载图片
					CmsResource resource = downloadImageFromUrl(src, site.getSiteId(), operator);
					if (Objects.nonNull(resource)) {
						imgTag = StringUtils.replaceEx(imgTag, src, resource.getInternalUrl());
					}
				}
			} catch (Exception e1) {
				String imgSrc = ImageUtils.isBase64Image(src) ? "base64Img" : src;
				log.warn("Save image failed: " + imgSrc);
				AsyncTaskManager.addErrMessage("Download remote image failed: " + imgSrc);
			}
			sb.append(imgTag);
			lastEndIndex = matcher.end();
		}
		sb.append(html.substring(lastEndIndex));
		return sb.toString();
	}

	@Override
	public boolean createThumbnailIfNotExists(InternalURL internalUrl, int width, int height) throws Exception {
		if (!InternalDataType_Resource.ID.equals(internalUrl.getType())
				|| !ResourceType_Image.isImage(internalUrl.getPath())) {
			return false;  // 非图片资源忽略
		}

		Long siteId = MapUtils.getLong(internalUrl.getParams(), InternalDataType_Resource.InternalUrl_Param_SiteId);
		CmsSite site = siteService.getSite(siteId);

		String filePath = internalUrl.getPath();
		String thumbnailPath = ImageUtils.getThumbnailFileName(filePath, width, height);
        String extension = FilenameUtils.getExtension(thumbnailPath);
        if ("svg".equalsIgnoreCase(extension)) {
            return false; // 不处理svg图片
        }

		String storageTypeId = FileStorageTypeProperty.getValue(site.getConfigProps());
		IFileStorageType fst = fileStorageService.getFileStorageType(storageTypeId);
		FileStorageHelper storageHelper = FileStorageHelper.of(fst, site);
		if (storageHelper.exists(thumbnailPath)) {
			return false;
		}
		CmsResource resource = this.getById(internalUrl.getId());
        if (Objects.isNull(resource)) {
            return false;
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (InputStream is = storageHelper.read(resource.getPath())) {
                String format = FileExUtils.getExtension(resource.getPath());
                ImageHelper.of(is, format).resize(width, height).to(os);
            }
            storageHelper.write(thumbnailPath, os.toByteArray());
        }
        return true;
	}

	@Override
	public void dealDefaultThumbnail(CmsSite site, List<String> internalUrls, Consumer<List<String>> consumer) {
		if (internalUrls.isEmpty()) {
			return;
		}
		int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
		int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
		if (w > 0 && h > 0) {
			List<String> thumbnails = dealThumbnails(internalUrls, w, h);
			consumer.accept(thumbnails);
		}
	}

	@Override
	public void dealDefaultThumbnail(CmsSite site, String internalUrl, Consumer<String> consumer) {
		if (StringUtils.isEmpty(internalUrl)) {
			return;
		}
		dealDefaultThumbnail(site, List.of(internalUrl), thumbnails -> consumer.accept(thumbnails.get(0)));
	}

	private List<String> dealThumbnails(List<String> internalUrls, int w, int h) {
		return internalUrls.stream().map(iurl -> {
			try {
				InternalURL internalURL = InternalUrlUtils.parseInternalUrl(iurl);
				if (Objects.nonNull(internalURL)) {
					// 先检查是否存在缩略图，如果不存在需要生成
                    boolean res = createThumbnailIfNotExists(internalURL, w, h);
                    String actualPreviewUrl = InternalUrlUtils.getActualPreviewUrl(internalURL);
                    if (res) {
                        // 返回缩略图路径
                        return ImageUtils.getThumbnailFileName(InternalUrlUtils.getActualPreviewUrl(internalURL), w, h);
                    } else {
                        // 返回源图路径
                        return actualPreviewUrl;
                    }
				}
				// 非内部链接返回原路径
				return iurl;
			} catch (Exception e) {
				log.error("Generate thumbnail fail: " + iurl, e);
				return iurl;
			}
		}).toList();
	}

    @Override
    public InputStream readResource(CmsResource resource) {
        CmsSite site = siteService.getSite(resource.getSiteId());
        String storageTypeId = FileStorageTypeProperty.getValue(site.getConfigProps());
        IFileStorageType fst = fileStorageService.getFileStorageType(storageTypeId);
        FileStorageHelper storageHelper = FileStorageHelper.of(fst, site);
        return storageHelper.read(resource.getPath());
    }

    /**
	 * 统计资源引用
	 */
	public void statResourceUsage(Long siteId) throws InterruptedException {
		Map<Long, Long> quotedResources = new HashMap<>();
		for (IResourceStat resourceStat : this.resourceStats) {
			resourceStat.statQuotedResource(siteId, quotedResources);
		}
	}
}
