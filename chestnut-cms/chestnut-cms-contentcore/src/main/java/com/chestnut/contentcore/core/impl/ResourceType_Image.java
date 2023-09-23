package com.chestnut.contentcore.core.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.chestnut.common.utils.file.ImageUtils;
import com.chestnut.contentcore.properties.ThumbnailHeightProperty;
import com.chestnut.contentcore.properties.ThumbnailWidthProperty;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.util.ThumbnailatorUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.WatermarkerPosition;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty;
import com.chestnut.contentcore.properties.ImageWatermarkArgsProperty.ImageWatermarkArgs;
import com.chestnut.contentcore.properties.ImageWatermarkProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.SiteUtils;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

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

	public final static String[] SuffixArray = { "jpg", "jpeg", "gif", "png", "ico", "webp" };

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
		String ext = FileNameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}

	@Override
	public byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		CmsSite site = siteService.getSite(resource.getSiteId());
		// 提取图片宽高属性
		try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
			BufferedImage bi = ImageIO.read(is);
			resource.setWidth(bi.getWidth());
			resource.setHeight(bi.getHeight());
			// 默认缩略图处理
			int w = ThumbnailWidthProperty.getValue(site.getConfigProps());
			int h = ThumbnailHeightProperty.getValue(site.getConfigProps());
			if (w > 0 && h > 0) {
				String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
				Thumbnails.of(bi).size(w, h).toFile(siteResourceRoot + ImageUtils.getThumbnailFileName(resource.getPath(), w, h));
			}
			// 添加水印
			if (ImageWatermarkProperty.getValue(site.getConfigProps())
					&& !"webp".equalsIgnoreCase(resource.getSuffix())) {
				// TODO webp水印支持
				ImageWatermarkArgs args = ImageWatermarkArgsProperty.getValue(site.getConfigProps());
				if (StringUtils.isNotEmpty(args.getImage())) {
					// 水印图片占比大小调整
					String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
					File file = new File(siteResourceRoot + args.getImage());
					if (file.exists()) {
						float waterremakImageWidth = bi.getWidth() * args.getRatio() * 0.01f;
						BufferedImage biWatermarkImage = ImageIO.read(file);
						biWatermarkImage = Thumbnails.of(biWatermarkImage)
								.scale(waterremakImageWidth / biWatermarkImage.getWidth()).asBufferedImage();
						try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
							// 添加水印
							Thumbnails.of(bi)
									.watermark(WatermarkerPosition.valueOf(args.getPosition()).position(),
											biWatermarkImage, args.getOpacity())
									.scale(1f).outputFormat(resource.getSuffix()).toOutputStream(os);
							bytes = os.toByteArray();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("图片处理失败：", e);
			resource.setWidth(0);
			resource.setHeight(0);
		}
		resource.setFileSize((long) bytes.length);
		return bytes;
	}
}
