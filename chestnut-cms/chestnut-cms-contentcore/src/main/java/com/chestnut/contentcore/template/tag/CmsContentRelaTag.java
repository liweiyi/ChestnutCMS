package com.chestnut.contentcore.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.mapper.CmsContentRelaMapper;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsContentRelaTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content_rela";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public final static String TagAttr_ContentId = "contentid";

	private final CmsContentRelaMapper contentRelaMapper;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(TagAttr_ContentId, true, TagAttrDataType.INTEGER, "内容ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, TagAttr_ContentId);
		if (contentId <= 0) {
			throw new TemplateException("内容ID错误：" + contentId, env);
		}
		Page<CmsContent> pageResult = contentRelaMapper
				.selectRelaContents(new Page<>(pageIndex, size, page), contentId, null);
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
