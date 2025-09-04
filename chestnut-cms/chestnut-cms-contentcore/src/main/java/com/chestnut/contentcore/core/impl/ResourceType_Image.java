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

import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.ImageHelper;
import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.common.utils.image.WatermarkPosition;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty.ImageWatermarkArgs;
import com.chestnut.contentcore.properties.ImageWatermarkProperty;
import com.chestnut.contentcore.properties.ThumbnailHeightProperty;
import com.chestnut.contentcore.properties.ThumbnailWidthProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ResourceUtils;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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
		String ext = FilenameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}

	@Override
	public List<File> process(CmsResource resource, File file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        if ("svg".equalsIgnoreCase(extension)) {
            return List.of(); // 不处理svg图片
        }
        CmsSite site = siteService.getSite(resource.getSiteId());
		boolean needWatermark = needWatermark(site);
		boolean needThumbnail = needThumbnail(site);
		if (!needWatermark && !needThumbnail) {
			return List.of(); // 不需要处理
		}
		List<File> files = new ArrayList<>();
		try {
			// 获取图片属性
			Dimension dimension = ImageUtils.getDimension(file);
			resource.setWidth(dimension.width);
			resource.setHeight(dimension.height);
			// 先处理图片水印
			if (needWatermark) {
				watermark(site, file);
				resource.setFileSize(file.length());
			}
			// 默认缩略图处理
			thumbnail(site, resource, file, files);
		} catch (IOException e) {
			log.error("Image resource process fail.", e);
		}
		return files;
	}

	private boolean needWatermark(CmsSite site) {
		if (!ImageWatermarkProperty.getValue(site.getConfigProps())) {
			return false;
		}
		ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
		if (StringUtils.isEmpty(args.getImage())) {
			return false;
		}
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		File file = new File(siteResourceRoot + args.getImage());
        return file.exists();
    }

	private void watermark(CmsSite site, File resourceFile) {
		ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		File wartermarkFile = new File(siteResourceRoot + args.getImage());
		try {
			ImageHelper.of(resourceFile).watermark(
					wartermarkFile,
					args.getRatio() * 0.01f,
					args.getOpacity(),
					WatermarkPosition.str2Position(args.getPosition())
			).toFile(resourceFile);
            log.debug("Watermark success: {}", resourceFile.getAbsolutePath());
		} catch (Exception e) {
			log.error("Watermark image fail.", e);
		}
	}

	private boolean needThumbnail(CmsSite site) {
		int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
		int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
		return w > 0 && h > 0;
	}

	private void thumbnail(CmsSite site, CmsResource resource, File tempFile, List<File> files) {
		// 读取存储配置
		int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
		int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
		if (w > 0 && h > 0) {
			// 生成默认缩略图
			String tempDirectory = ResourceUtils.getResourceTempDirectory(site);
			String thumbnailPath = ImageUtils.getThumbnailFileName(resource.getPath(), w, h);
			File output = new File(tempDirectory + thumbnailPath);
            try {
				ImageHelper.of(tempFile).resize(w, h).toFile(output);
				files.add(output);
				log.debug("Thumbnail success: {}", output.getAbsolutePath());
            } catch (Exception e) {
                try {
					// 生成缩略图失败直接使用源图作为缩略图
                    Files.copy(tempFile.toPath(), Path.of(thumbnailPath), StandardCopyOption.REPLACE_EXISTING);
					files.add(output);
                } catch (IOException ex) {
                    log.error("Copy thumbnail file err.", ex);
                }
            }
        }
	}
}
