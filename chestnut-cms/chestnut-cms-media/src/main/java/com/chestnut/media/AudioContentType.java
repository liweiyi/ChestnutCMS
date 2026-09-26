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
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.BCmsAudio;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.dto.AudioAlbumDTO;
import com.chestnut.media.domain.vo.AudioAlbumVO;
import com.chestnut.media.mapper.BCmsAudioMapper;
import com.chestnut.media.service.IAudioService;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Component(IContentType.BEAN_NAME_PREFIX + AudioContentType.ID)
@RequiredArgsConstructor
public class AudioContentType implements IContentType {

	public final static String ID = "audio";
    
    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

	private final ISiteService siteService;

	private final IContentService contentService;

	private final IAudioService audioService;

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
		return 3;
	}

	@Override
	public String getComponent() {
		return "cms/audioAlbum/editor";
	}

	@Override
	public IContent<?> newContent() {
		return new AudioContent();
	}

	@Override
	public IContent<?> loadContent(CmsContent xContent) {
		AudioContent audioContent = new AudioContent();
		audioContent.setContentEntity(xContent);
		return audioContent;
	}

	@Override
	public ContentDTO parseRequest(InputStream is) {
		return JacksonUtils.from(is, AudioAlbumDTO.class);
	}

	@Override
	public IContent<?> dto2content(ContentDTO dto) {
		if (dto instanceof AudioAlbumDTO _dto) {
			return readFrom0(_dto);
		}
		throw ContentCoreErrorCode.DTO_NOT_MATCH_CONTENT_TYPE.exception();
	}

	private AudioContent readFrom0(AudioAlbumDTO dto) {
		// 内容基础信息
		CmsContent contentEntity = dto.convertToContentEntity(this.catalogService, this.contentService);
		// 音频扩展信息
		List<CmsAudio> audioList = dto.getAudioList();

		AudioContent content = new AudioContent();
		content.setContentEntity(contentEntity);
		content.setExtendEntity(audioList);
		content.setParams(dto.getParams());
		return content;
	}

	@Override
	public ContentVO initEditor(CmsCatalog catalog, CmsContent contentEntity) {
		AudioAlbumVO vo;
		if (Objects.nonNull(contentEntity)) {
			List<CmsAudio> list = this.audioService.dao().lambdaQuery().eq(CmsAudio::getContentId, contentEntity.getContentId())
					.orderByAsc(CmsAudio::getSortFlag).list();
			list.forEach(audio -> {
				audio.setSrc(InternalUrlUtils.getActualPreviewUrl(audio.getPath()));
                audio.setCoverSrc(InternalUrlUtils.getActualPreviewUrl(audio.getCover()));
				audio.setFileSizeName(FileUtils.byteCountToDisplaySize(audio.getFileSize()));
			});
			vo = AudioAlbumVO.newInstance(contentEntity, list);
		} else {
			vo = new AudioAlbumVO();
		}
		return vo;
	}

	@Override
	public void recover(BCmsContent backupContent) {
		this.contentService.dao().recover(backupContent);

		if (!YesOrNo.isYes(backupContent.getLinkFlag()) && !ContentCopyType.isMapping(backupContent.getCopyType())) {
			BCmsAudioMapper backupMapper = this.audioService.dao().getBackupMapper();
			List<BCmsAudio> backupImages = backupMapper.selectList(new LambdaQueryWrapper<BCmsAudio>()
					.eq(BCmsAudio::getContentId, backupContent.getContentId())
					.eq(BCmsAudio::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));

			backupImages.forEach(backupImage -> this.audioService.dao().recover(backupImage));
		}
	}

	@Override
	public void deleteBackups(Long contentId) {
		this.contentService.dao().deleteBackups(new LambdaQueryWrapper<BCmsContent>()
				.eq(BCmsContent::getContentId, contentId)
				.eq(BCmsContent::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
		this.audioService.dao().deleteBackups(new LambdaQueryWrapper<BCmsAudio>()
				.eq(BCmsAudio::getContentId, contentId)
				.eq(BCmsAudio::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
	}
}
