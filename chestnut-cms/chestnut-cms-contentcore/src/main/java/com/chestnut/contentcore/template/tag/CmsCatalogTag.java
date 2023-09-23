package com.chestnut.contentcore.template.tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.chestnut.common.staticize.tag.TagAttrOption;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.template.exception.CatalogNotFoundException;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsCatalogTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_catalog";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private static final String TAG_ATTR_ID = "id";
	private static final String TAG_ATTR_ALIAS = "alias";
	private static final String TAG_ATTR_LEVEL = "level";

	private final ICatalogService catalogService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(TAG_ATTR_ID, false, TagAttrDataType.INTEGER, "栏目ID"));
		tagAttrs.add(new TagAttr(TAG_ATTR_ALIAS, false, TagAttrDataType.STRING, "栏目别名"));
		tagAttrs.add(new TagAttr(TAG_ATTR_LEVEL, false, TagAttrDataType.STRING, "数据获取范围，值为`Root`时忽略属性id、alias",
				CatalogTagLevel.toTagAttrOptions(), CatalogTagLevel.Current.name()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long catalogId = MapUtils.getLongValue(attrs, TAG_ATTR_ID);
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);

		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		String alias = MapUtils.getString(attrs, TAG_ATTR_ALIAS);
		if (Objects.isNull(catalog) && StringUtils.isNotEmpty(alias)) {
			catalog = this.catalogService.getCatalogByAlias(siteId, alias);
		}
		String level = MapUtils.getString(attrs, TAG_ATTR_LEVEL);
		if (!CatalogTagLevel.isRoot(level) && Objects.isNull(catalog)) {
			throw new CatalogNotFoundException(getTagName(), catalogId, alias, env);
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<>();
		q.eq(CmsCatalog::getSiteId, siteId).eq(CmsCatalog::getVisibleFlag, YesOrNo.YES);
		if (CatalogTagLevel.isCurrent(level)) {
			q.eq(CmsCatalog::getParentId, catalog.getParentId());
		} else if (CatalogTagLevel.isChild(level)) {
			q.eq(CmsCatalog::getParentId, catalog.getCatalogId());
		} else if (CatalogTagLevel.isCurrentAndChild(level)) {
			q.likeRight(CmsCatalog::getAncestors, catalog.getAncestors());
		} else if (CatalogTagLevel.isSelf(level)) {
			q.eq(CmsCatalog::getCatalogId, catalog.getCatalogId());
		}
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsCatalog::getSortFlag);

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsCatalog> pageResult = this.catalogService.page(new Page<>(pageIndex, size, page), q);
		pageResult.getRecords().forEach(c -> {
			c.setLink(catalogService.getCatalogLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			c.setListLink(catalogService.getCatalogListLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
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

	private enum CatalogTagLevel {
		Root("所有栏目"), Current("同级栏目"), Child("子栏目"), CurrentAndChild("当前栏目及子栏目"), Self("当前栏目");

		private final String desc;

		CatalogTagLevel(String desc) {
			this.desc = desc;
		}

		static boolean isRoot(String level) {
			return Root.name().equalsIgnoreCase(level);
		}

		static boolean isCurrent(String level) {
			return Current.name().equalsIgnoreCase(level);
		}

		static boolean isChild(String level) {
			return Child.name().equalsIgnoreCase(level);
		}

		static boolean isCurrentAndChild(String level) {
			return CurrentAndChild.name().equalsIgnoreCase(level);
		}

		static boolean isSelf(String level) {
			return Self.name().equalsIgnoreCase(level);
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Root.name(), Root.desc),
					new TagAttrOption(Current.name(), Current.desc),
					new TagAttrOption(Child.name(), Child.desc),
					new TagAttrOption(CurrentAndChild.name(), CurrentAndChild.desc),
					new TagAttrOption(Self.name(), Self.desc)
			);
		}
	}
}
