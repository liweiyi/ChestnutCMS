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
package com.chestnut.media;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.db.DBConstants;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.BCmsVideo;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.domain.dto.VideoAlbumDTO;
import com.chestnut.media.domain.vo.VideoAlbumVO;
import com.chestnut.media.mapper.BCmsVideoMapper;
import com.chestnut.media.service.IVideoService;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component(IContentType.BEAN_NAME_PREFIX + VideoContentType.ID)
@RequiredArgsConstructor
public class VideoContentType implements IContentType {

	public final static String ID = "video";
    
    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

	private final ISiteService siteService;

	private final IContentService contentService;

	private final IVideoService videoService;
	
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
		return 4;
	}

	@Override
	public String getComponent() {
		return "cms/videoAlbum/editor";
	}

	@Override
	public IContent<?> newContent() {
		return new VideoContent();
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		VideoContent videoContent = new VideoContent();
		videoContent.setContentEntity(xContent);
		return videoContent;
	}

	@Override
	public IContent<?> readFrom(InputStream is) {
		VideoAlbumDTO dto = JacksonUtils.from(is, VideoAlbumDTO.class);
		return readFrom0(dto);
	}

	private VideoContent readFrom0(VideoAlbumDTO dto) {
		// 内容基础信息
		CmsContent contentEntity = dto.convertToContentEntity(this.catalogService, this.contentService);
		// 视频扩展信息
		List<CmsVideo> videoList = dto.getVideoList();

		VideoContent content = new VideoContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(videoList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(Long catalogId, Long contentId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		VideoAlbumVO vo;
		if (IdUtils.validate(contentId)) {
			CmsContent contentEntity = this.contentService.dao().getById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			List<CmsVideo> list = this.videoService.dao().lambdaQuery().eq(CmsVideo::getContentId, contentId)
					.orderByAsc(CmsVideo::getSortFlag).list();
			list.forEach(video -> {
				video.setSrc(InternalUrlUtils.getActualPreviewUrl(video.getPath()));
				video.setFileSizeName(FileUtils.byteCountToDisplaySize(video.getFileSize()));
				if (StringUtils.isNotEmpty(video.getCover())) {
					video.setCoverSrc(InternalUrlUtils.getActualPreviewUrl(video.getCover()));
				}
			});
			vo = VideoAlbumVO.newInstance(contentEntity, list);
			// 发布通道模板数据
			List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					PublishPipePropUseType.Content, contentEntity.getPublishPipeProps());
			vo.setPublishPipeProps(publishPipeProps);
		} else {
			vo = new VideoAlbumVO();
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
			BCmsVideoMapper backupMapper = this.videoService.dao().getBackupMapper();
			List<BCmsVideo> backups = backupMapper.selectList(new LambdaQueryWrapper<BCmsVideo>()
					.eq(BCmsVideo::getContentId, backupContent.getContentId())
					.eq(BCmsVideo::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));

			backups.forEach(backupImage -> this.videoService.dao().recover(backupImage));
		}
	}

	@Override
	public void deleteBackups(Long contentId) {
		this.contentService.dao().deleteBackups(new LambdaQueryWrapper<BCmsContent>()
				.eq(BCmsContent::getContentId, contentId)
				.eq(BCmsContent::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
		this.videoService.dao().deleteBackups(new LambdaQueryWrapper<BCmsVideo>()
				.eq(BCmsVideo::getContentId, contentId)
				.eq(BCmsVideo::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
	}
}
