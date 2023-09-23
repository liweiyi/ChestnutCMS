package com.chestnut.media;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.enums.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.domain.dto.VideoAlbumDTO;
import com.chestnut.media.domain.vo.VideoAlbumVO;
import com.chestnut.media.mapper.CmsVideoMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(IContentType.BEAN_NAME_PREFIX + VideoContentType.ID)
@RequiredArgsConstructor
public class VideoContentType implements IContentType {

	public final static String ID = "video";
    
    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

	private final CmsContentMapper contentMapper;

	private final CmsVideoMapper videoMapper;
	
	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

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
	public IContent<?> readRequest(HttpServletRequest request) throws IOException {
		VideoAlbumDTO dto = JacksonUtils.from(request.getInputStream(), VideoAlbumDTO.class);

		CmsContent contentEntity;
		if (dto.getOpType() == ContentOpType.UPDATE) {
			contentEntity = this.contentMapper.selectById(dto.getContentId());
			Assert.notNull(contentEntity,
					() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getContentId()));
		} else {
			contentEntity = new CmsContent();
		}
		BeanUtils.copyProperties(dto, contentEntity);
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		contentEntity.setSiteId(catalog.getSiteId());
		contentEntity.setAttributes(ContentAttribute.convertInt(dto.getAttributes()));
		// 发布通道配置
		Map<String, Map<String, Object>> publishPipProps = new HashMap<>();
		dto.getPublishPipeProps().forEach(prop -> {
			publishPipProps.put(prop.getPipeCode(), prop.getProps());
		});
		contentEntity.setPublishPipeProps(publishPipProps);

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
			CmsContent contentEntity = this.contentMapper.selectById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			List<CmsVideo> list = new LambdaQueryChainWrapper<>(this.videoMapper).eq(CmsVideo::getContentId, contentId)
					.orderByAsc(CmsVideo::getSortFlag).list();
			list.forEach(video -> {
				video.setSrc(InternalUrlUtils.getActualPreviewUrl(video.getPath()));
				video.setFileSizeName(FileUtils.byteCountToDisplaySize(video.getFileSize()));
				if (StringUtils.isNotEmpty(video.getCover())) {
					video.setCoverSrc(InternalUrlUtils.getActualPreviewUrl(video.getCover()));
				}
			});
			vo = VideoAlbumVO.newInstance(contentEntity, list);
			if (StringUtils.isNotEmpty(vo.getLogo())) {
				vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
			}
			// 发布通道模板数据
			List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
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
			List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					PublishPipePropUseType.Content, null);
			vo.setPublishPipeProps(publishPipeProps);
		}
		vo.setCatalogName(catalog.getName());
		return vo;
	}

	@Override
	public void recover(Long contentId) {
		this.videoMapper.recoverByContentId(contentId);
	}

	@Override
	public void deleteBackups(Long contentId) {
		this.videoMapper.deleteLogicDeletedByContentId(contentId);
	}
}
