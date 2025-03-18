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
package com.chestnut.contentcore.core.impl;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.common.utils.image.ImageHelper;
import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.common.utils.image.WatermarkPosition;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.*;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty.ImageWatermarkArgs;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.FileStorageHelper;
import com.chestnut.contentcore.util.ResourceUtils;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 资源类型：图片
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_Image.ID)
public class ResourceType_Image implements IResourceType {

	public final static String ID = "image";
	
	public static final  String NAME = "{CMS.CONTENTCORE.RESOURCE_TYPE." + ID + "}";

	public final static String[] SuffixArray = { "jpg", "jpeg", "gif", "png", "ico", "webp", "svg" };

	private final ISiteService siteService;

	private final IResourceService resourceService;

	private final Map<String, IFileStorageType> fileStorageTypeMap;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}
	
	public static boolean isImage(String path) {
		String ext = FileNameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}

	@Override
	public void asyncProcess(CmsResource resource) {
		asyncTaskManager.execute(() -> {
			CmsSite site = siteService.getSite(resource.getSiteId());
			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
			IFileStorageType fst = fileStorageTypeMap.get(IFileStorageType.BEAN_NAME_PREIFX + fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
			String tempDirectory = ResourceUtils.getResourceTempDirectory(site);
			File tempFile = new File(tempDirectory + resource.getPath());
			FileExUtils.mkdirs(tempFile.getParent());
			Map<String, File> paths = new HashMap<>();
			try (InputStream read = fileStorageHelper.read(resource.getPath())) {
				FileExUtils.transfer(read, tempFile);
				// 获取图片属性
				BufferedImage bi = ImageIO.read(tempFile);
				resource.setWidth(bi.getWidth());
				resource.setHeight(bi.getHeight());
				// 先处理图片水印
				if (watermark(site, bi, tempFile)) {
					paths.put(resource.getPath(), tempFile);
					resource.setFileSize(tempFile.length());
				}
				// 默认缩略图处理
				thumbnail(site, resource, tempFile, paths);
			} catch (IOException e) {
				log.error("Image resource process fail.", e);
			} finally {
				for (Map.Entry<String, File> entry : paths.entrySet()) {
					try {
						Path path = entry.getValue().toPath();
						try (InputStream is = Files.newInputStream(path)) {
							fileStorageHelper.write(entry.getKey(), is);
						}
						// 删除临时图片文件
						Files.deleteIfExists(path);
					} catch (IOException e) {
						log.error("Delete temp file failed.", e);
					}
				}
				resourceService.updateById(resource);
			}
		});
	}

	private boolean watermark(CmsSite site, BufferedImage bi, File output) {
		if (!ImageWatermarkProperty.getValue(site.getConfigProps())) {
			return false;
		}
		ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
		if (StringUtils.isEmpty(args.getImage())) {
			return false;
		}
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		File file = new File(siteResourceRoot + args.getImage());
		if (!file.exists()) {
			return false;
		}
		try {
			BufferedImage biWatermarkImage = ImageIO.read(file);
			String ext = FilenameUtils.getExtension(output.getName());
			ImageHelper.of(bi).format(ext).watermark(
					biWatermarkImage,
					args.getRatio() * 0.01f,
					args.getOpacity(),
					WatermarkPosition.str2Position(args.getPosition())
			).toFile(output);
            log.debug("Watermark success: {}", output.getAbsolutePath());
			return true;
		} catch (IOException e) {
			log.error("Watermark image fail.", e);
		}
		return false;
	}

	private void thumbnail(CmsSite site, CmsResource resource, File tempFile, Map<String, File> paths) {
		// 读取存储配置
		int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
		int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
		if (w > 0 && h > 0) {
			// 生成默认缩略图
			String tempDirectory = ResourceUtils.getResourceTempDirectory(site);
			String ext = FilenameUtils.getExtension(resource.getFileName());
			String thumbnailPath = ImageUtils.getThumbnailFileName(resource.getPath(), w, h);
			File output = new File(tempDirectory + thumbnailPath);
            try {
				ImageHelper.of(tempFile).format(ext).resize(w, h).toFile(output);
				paths.put(thumbnailPath, output);
				log.debug("Thumbnail success: {}", output.getAbsolutePath());
            } catch (IOException e) {
                try {
					// 生成缩略图失败直接使用源图作为缩略图
                    Files.copy(tempFile.toPath(), Path.of(thumbnailPath), StandardCopyOption.REPLACE_EXISTING);
					paths.put(thumbnailPath, output);
                } catch (IOException ex) {
                    log.error("Copy thumbnail file err.", ex);
                }
            }
        }
	}
}
