package com.chestnut.cms.image.template.tag;

import java.util.List;
import java.util.Map;

import com.chestnut.common.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.service.IImageService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.InternalUrlUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CmsImageTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_image";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IContentService contentService;

	private final IImageService imageService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("contentid", true, TagAttrDataType.INTEGER, "图集内容ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, "contentid", 0);
		if (contentId <= 0) {
			throw new TemplateException("图集内容ID错误：" + contentId, env);
		}
		CmsContent c = this.contentService.getById(contentId);
		if (ContentCopyType.isMapping(c.getCopyType())) {
			contentId = c.getCopyId();
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);

		LambdaQueryWrapper<CmsImage> q = new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId, contentId);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		Page<CmsImage> pageResult = this.imageService.page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().size() == 0) {
			throw new TemplateException("内容列表页码超出上限：" + pageIndex, env);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		pageResult.getRecords().forEach(image -> {
			image.setSrc(
					InternalUrlUtils.getActualUrl(image.getPath(), context.getPublishPipeCode(), context.isPreview()));
		});
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}
}
