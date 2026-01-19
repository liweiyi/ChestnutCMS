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
package com.chestnut.cms.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.cms.image.domain.BCmsImage;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.domain.dto.ImageAlbumDTO;
import com.chestnut.cms.image.domain.vo.ImageAlbumVO;
import com.chestnut.cms.image.mapper.BCmsImageMapper;
import com.chestnut.cms.image.service.IImageService;
import com.chestnut.common.db.DBConstants;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component(IContentType.BEAN_NAME_PREFIX + ImageContentType.ID)
@RequiredArgsConstructor
public class ImageContentType implements IContentType {

	public final static String ID = "image";
    
    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

	private final ISiteService siteService;

	private final IContentService contentService;

	private final IImageService imageService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IResourceService resourceService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return 2;
	}

	@Override
	public String getComponent() {
		return "cms/imageAlbum/editor";
	}

	@Override
	public IContent<?> newContent() {
		return new ImageContent();
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		ImageContent imageContent = new ImageContent();
		imageContent.setContentEntity(xContent);
		return imageContent;
	}

	@Override
	public IContent<?> readFrom(InputStream is) {
		ImageAlbumDTO dto = JacksonUtils.from(is, ImageAlbumDTO.class);
		return readFrom0(dto);
	}

	private ImageContent readFrom0(ImageAlbumDTO dto) {
		// 内容基础信息
		CmsContent contentEntity = dto.convertToContentEntity(this.catalogService, this.contentService);
		// 图集扩展信息
		List<CmsImage> imageList = dto.getImageList();

		ImageContent content = new ImageContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(imageList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(Long catalogId, Long contentId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		ImageAlbumVO vo;
		if (IdUtils.validate(contentId)) {
			CmsContent contentEntity = this.contentService.dao().getById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, contentId)
					.orderByAsc(CmsImage::getSortFlag);
			List<CmsImage> list = this.imageService.dao().list(q);
			list.forEach(img -> {
				img.setSrc(InternalUrlUtils.getActualPreviewUrl(img.getPath()));
				img.setFileSizeName(FileUtils.byteCountToDisplaySize(img.getFileSize()));
			});
			vo = ImageAlbumVO.newInstance(contentEntity, list);
			// 发布通道模板数据
			List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					PublishPipePropUseType.Content, contentEntity.getPublishPipeProps());
			vo.setPublishPipeProps(publishPipeProps);
		} else {
			vo = new ImageAlbumVO();
			vo.setContentId(IdUtils.getSnowflakeId());
			vo.setCatalogId(catalog.getCatalogId());
			vo.setContentType(ID);
			// 发布通道初始数据
			vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
			// 发布通道模板数据
			List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					PublishPipePropUseType.Content, null);
			vo.setPublishPipeProps(publishPipeProps);
		}
		vo.setCatalogName(catalog.getName());
		// 内容引导图缩略图处理
		CmsSite site = siteService.getSite(catalog.getSiteId());
		resourceService.dealDefaultThumbnail(site, vo.getImages(), thumbnails -> {
			vo.setImagesSrc(thumbnails);
			vo.setLogoSrc(thumbnails.get(0));
		});
		return vo;
	}

	@Override
	public void recover(BCmsContent backupContent) {
		this.contentService.dao().recover(backupContent);

		if (!YesOrNo.isYes(backupContent.getLinkFlag()) && !ContentCopyType.isMapping(backupContent.getCopyType())) {
			BCmsImageMapper backupMapper = this.imageService.dao().getBackupMapper();
			List<BCmsImage> backupImages = backupMapper.selectList(new LambdaQueryWrapper<BCmsImage>()
					.eq(BCmsImage::getContentId, backupContent.getContentId())
					.eq(BCmsImage::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));

			backupImages.forEach(backupImage -> this.imageService.dao().recover(backupImage));
		}
	}

	@Override
	public void deleteBackups(Long contentId) {
		this.contentService.dao().deleteBackups(new LambdaQueryWrapper<BCmsContent>()
				.eq(BCmsContent::getContentId, contentId)
				.eq(BCmsContent::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
		this.imageService.dao().deleteBackups(new LambdaQueryWrapper<BCmsImage>()
				.eq(BCmsImage::getContentId, contentId)
				.eq(BCmsImage::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
	}
}
