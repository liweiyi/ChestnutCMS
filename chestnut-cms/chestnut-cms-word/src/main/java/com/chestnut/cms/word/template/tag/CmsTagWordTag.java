package com.chestnut.cms.word.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.service.ITagWordGroupService;
import com.chestnut.word.service.ITagWordService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CmsTagWordTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_tag_word";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final ITagWordGroupService tagWordGroupService;

	private final ITagWordService tagWordService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("group", true, TagAttrDataType.STRING, "TAG词分组编码") );
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String group = MapUtils.getString(attrs, "group");
		Optional<TagWordGroup> opt = tagWordGroupService.lambdaQuery().eq(TagWordGroup::getCode, group).oneOpt();
		if (opt.isEmpty()) {
			throw new TemplateException("Tag word group not found: " + group, env);
		}
		TagWordGroup tagWordGroup = opt.get();
		LambdaQueryWrapper<TagWord> q = new LambdaQueryWrapper<TagWord>()
				.eq(TagWord::getGroupId, tagWordGroup.getGroupId());

		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(TagWord::getSortFlag);

		 Page<TagWord> pageResult = this.tagWordService.page(new Page<>(pageIndex, size, page), q);
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
