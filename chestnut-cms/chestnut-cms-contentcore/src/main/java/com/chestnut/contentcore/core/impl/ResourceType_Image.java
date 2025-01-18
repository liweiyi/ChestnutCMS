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
package com.chestnut.contentcore.core.impl;

import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.ImageHelper;
import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.common.utils.image.WatermarkPosition;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.*;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty.ImageWatermarkArgs;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.FileStorageHelper;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
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

	private final Map<String, IFileStorageType> fileStorageTypeMap;

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
	public byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		CmsSite site = siteService.getSite(resource.getSiteId());
		try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
			// 提取图片宽高属性
			BufferedImage bi = ImageIO.read(is);
			resource.setWidth(bi.getWidth());
			resource.setHeight(bi.getHeight());
			// 添加水印
			if (ImageWatermarkProperty.getValue(site.getConfigProps())) {
				ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
				if (StringUtils.isNotEmpty(args.getImage())) {
					String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
					File file = new File(siteResourceRoot + args.getImage());
					if (file.exists()) {
						BufferedImage biWatermarkImage = ImageIO.read(file);
						try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
							String ext = FilenameUtils.getExtension(resource.getFileName());
							ImageHelper.of(bi).format(ext).watermark(
									biWatermarkImage,
									args.getRatio() * 0.01f,
									args.getOpacity(),
									WatermarkPosition.str2Position(args.getPosition())
							).to(os);
							bytes = os.toByteArray();
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("Read image failed: " + resource.getPath(), e);
			resource.setWidth(0);
			resource.setHeight(0);
		}
		resource.setFileSize((long) bytes.length);
		return bytes;
	}

	@Override
	public void afterProcess(CmsResource resource) {
		CmsSite site = siteService.getSite(resource.getSiteId());
		int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
		int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
		if (w > 0 && h > 0) {
			// 读取存储配置
			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
			IFileStorageType fst = fileStorageTypeMap.get(IFileStorageType.BEAN_NAME_PREIFX + fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
			// 生成默认缩略图
			String ext = FilenameUtils.getExtension(resource.getFileName());
			String thumbnailPath = ImageUtils.getThumbnailFileName(resource.getPath(), w, h);
			InputStream read = fileStorageHelper.read(resource.getPath());
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				ImageHelper.of(read).format(ext).resize(w, h).to(bos);
				fileStorageHelper.write(thumbnailPath, bos.toByteArray());
			} catch (IOException e) {
                log.warn("Generate default thumbnail image failed: " + resource.getPath(), e);
				// 生成缩略图失败直接使用源图作为缩略图
				fileStorageHelper.write(thumbnailPath, read);
            } finally {
				try {
					if (Objects.nonNull(read)) {
						read.close();
                    }
				} catch (IOException e) {
                    log.warn("Input stream close err!", e);
                }
            }
        }
	}
}
