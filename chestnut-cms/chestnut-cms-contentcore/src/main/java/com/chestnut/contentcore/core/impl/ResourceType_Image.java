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
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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
	public List<File> process(CmsResource resource, File file) throws IOException {
		CmsSite site = siteService.getSite(resource.getSiteId());
		boolean needWatermark = needWatermark(site);
		boolean needThumbnail = needThumbnail(site);
		if (!needWatermark && !needThumbnail) {
			return List.of(); // 不需要处理
		}
		List<File> files = new ArrayList<>();
		try {
			// 获取图片属性
			BufferedImage bi = ImageIO.read(file);
			resource.setWidth(bi.getWidth());
			resource.setHeight(bi.getHeight());
			// 先处理图片水印
			if (needWatermark) {
				watermark(site, bi, file);
				resource.setFileSize(file.length());
			}
			// 默认缩略图处理
			thumbnail(site, resource, file, files);
		} catch (IOException e) {
			log.error("Image resource process fail.", e);
		}
		return files;
	}

//	@Override
//	public void asyncProcess(CmsResource resource) {
//		CmsSite site = siteService.getSite(resource.getSiteId());
//		boolean needWatermark = needWatermark(site);
//		boolean needThumbnail = needThumbnail(site);
//		if (!needWatermark && !needThumbnail) {
//			return; // 不需要处理
//		}
//		asyncTaskManager.execute(() -> {
//			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
//			IFileStorageType fst = fileStorageTypeMap.get(IFileStorageType.BEAN_NAME_PREIFX + fileStorageType);
//			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
//			String tempDirectory = ResourceUtils.getResourceTempDirectory(site);
//			File tempFile = new File(tempDirectory + resource.getPath());
//			FileExUtils.mkdirs(tempFile.getParent());
//			Map<String, File> paths = new HashMap<>();
//			try (InputStream read = fileStorageHelper.read(resource.getPath())) {
//				FileExUtils.transfer(read, tempFile);
//				// 获取图片属性
//				BufferedImage bi = ImageIO.read(tempFile);
//				resource.setWidth(bi.getWidth());
//				resource.setHeight(bi.getHeight());
//				// 先处理图片水印
//				if (needWatermark) {
//					watermark(site, bi, tempFile);
//					paths.put(resource.getPath(), tempFile);
//					resource.setFileSize(tempFile.length());
//				}
//				// 默认缩略图处理
//				thumbnail(site, resource, tempFile);
//				// 更新文件
//				for (Map.Entry<String, File> entry : paths.entrySet()) {
//					try (FileInputStream is = new FileInputStream(entry.getValue())) {
//						fileStorageHelper.write(entry.getKey(), is, entry.getValue().length());
//					} catch (IOException e) {
//						log.error("Write temp file to storage {} failed.", fst.getType(), e);
//					}
//				}
//				if (needWatermark) {
//					resourceService.updateById(resource);
//				}
//			} catch (IOException e) {
//				log.error("Image resource process fail.", e);
//			} finally {
//				try {
//					// 删除临时文件
//					Files.deleteIfExists(tempFile.toPath());
//                    for (File file : paths.values()) {
//                        Files.deleteIfExists(file.toPath());
//                    }
//				} catch (IOException e) {
//					log.error("Delete temp file failed.", e);
//				}
//			}
//		});
//	}

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

	private void watermark(CmsSite site, BufferedImage bi, File output) {
		ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		File file = new File(siteResourceRoot + args.getImage());
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
		} catch (IOException e) {
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
			String ext = FilenameUtils.getExtension(resource.getFileName());
			String thumbnailPath = ImageUtils.getThumbnailFileName(resource.getPath(), w, h);
			File output = new File(tempDirectory + thumbnailPath);
            try {
				ImageHelper.of(tempFile).format(ext).resize(w, h).toFile(output);
				files.add(output);
				log.debug("Thumbnail success: {}", output.getAbsolutePath());
            } catch (IOException e) {
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
