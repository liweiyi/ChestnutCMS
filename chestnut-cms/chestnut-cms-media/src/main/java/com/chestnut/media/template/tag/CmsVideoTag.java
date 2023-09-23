package com.chestnut.media.template.tag;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IVideoService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CmsVideoTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_video";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IContentService contentService;

	private final IVideoService videoService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("contentid", true, TagAttrDataType.INTEGER, "视频集内容ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, "contentid", 0);
		if (contentId <= 0) {
			throw new TemplateException("视频集内容ID错误：" + contentId, env);
		}
		CmsContent c = this.contentService.getById(contentId);
		if (ContentCopyType.isMapping(c.getCopyType())) {
			contentId = c.getCopyId();
		}

		LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getContentId, contentId);
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsVideo::getSortFlag);

		Page<CmsVideo> pageResult = this.videoService.page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().size() == 0) {
			throw new TemplateException("内容列表页码超出上限：" + pageIndex, env);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		pageResult.getRecords().forEach(video -> {
			video.setSrc(
					InternalUrlUtils.getActualUrl(video.getPath(), context.getPublishPipeCode(), context.isPreview()));
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
