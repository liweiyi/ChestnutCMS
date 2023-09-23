package com.chestnut.cms.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.domain.dto.ImageAlbumDTO;
import com.chestnut.cms.image.domain.vo.ImageAlbumVO;
import com.chestnut.cms.image.mapper.CmsImageMapper;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(IContentType.BEAN_NAME_PREFIX + ImageContentType.ID)
@RequiredArgsConstructor
public class ImageContentType implements IContentType {

	public final static String ID = "image";
    
    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

	private final CmsContentMapper contentMapper;

	private final CmsImageMapper imageMapper;

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
	public IContent<?> readRequest(HttpServletRequest request) throws IOException {
		ImageAlbumDTO dto = JacksonUtils.from(request.getInputStream(), ImageAlbumDTO.class);

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
			CmsContent contentEntity = this.contentMapper.selectById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, contentId)
					.orderByAsc(CmsImage::getSortFlag);
			List<CmsImage> list = this.imageMapper.selectList(q);
			list.forEach(img -> {
				img.setSrc(InternalUrlUtils.getActualPreviewUrl(img.getPath()));
				img.setFileSizeName(FileUtils.byteCountToDisplaySize(img.getFileSize()));
			});
			vo = ImageAlbumVO.newInstance(contentEntity, list);
			if (StringUtils.isNotEmpty(vo.getLogo())) {
				vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
			}
			// 发布通道模板数据
			List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
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
			List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					PublishPipePropUseType.Content, null);
			vo.setPublishPipeProps(publishPipeProps);
		}
		vo.setCatalogName(catalog.getName());
		return vo;
	}

	@Override
	public void recover(Long contentId) {
		this.imageMapper.recoverByContentId(contentId);
	}

	@Override
	public void deleteBackups(Long contentId) {
		this.imageMapper.deleteLogicDeletedByContentId(contentId);
	}
}
