package com.chestnut.cms.search.template.tag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.cms.search.service.ContentIndexService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.search.SearchConsts;
import com.fasterxml.jackson.databind.node.ObjectNode;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CmsRelaContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_rela_content";

	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ElasticsearchClient esClient;

	private final ContentIndexService searchService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("catalogid", false, TagAttrDataType.INTEGER, "栏目ID"));
		tagAttrs.add(new TagAttr("keywords", false, TagAttrDataType.STRING, "关键词"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		long catalogId = MapUtils.getLongValue(attrs, "catalogid", 0);
		String keywords = StringUtils.replaceEx(MapUtils.getString(attrs, "keywords"), ",", " ");
		if (this.searchService.isElasticSearchAvailable() && StringUtils.isNotEmpty(keywords)) {
			try {
				List<ContentDTO> list = findContentByIndex(siteId, catalogId, keywords, size);
				return TagPageData.of(list, list.size());
			} catch (IOException e) {
				throw new TemplateException("Find rela content from es failed.", e, env);
			}
		} else {
			List<CmsContent> contents = this.findContentByDB(siteId, catalogId, size);
			TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
			List<ContentDTO> list = contents.stream().map(c -> {
				ContentDTO dto = ContentDTO.newInstance(c);
				dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
				return dto;
			}).toList();
			return TagPageData.of(list, list.size());
		}
	}

	/**
	 * 数据库随机
	 */
	private List<CmsContent> findContentByDB(long siteId, long catalogId, int size) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		boolean isCatalogNonNull = Objects.nonNull(catalog);
		Page<CmsContent> pageMin = this.contentService.lambdaQuery().select(List.of(CmsContent::getContentId))
				.eq(CmsContent::getSiteId, siteId)
				.likeRight(isCatalogNonNull, CmsContent::getCatalogAncestors, catalog.getAncestors())
				.orderByAsc(CmsContent::getContentId)
				.page(new Page<>(1, 1, false));
		if (pageMin.getRecords().isEmpty()) {
			return List.of();
		}
		Long minContentId = pageMin.getRecords().get(0).getContentId();
		Page<CmsContent> pageMax = this.contentService.lambdaQuery().select(List.of(CmsContent::getContentId))
				.eq(CmsContent::getSiteId, siteId)
				.likeRight(isCatalogNonNull, CmsContent::getCatalogAncestors, catalog.getAncestors())
				.orderByDesc(CmsContent::getContentId)
				.page(new Page<>(size, 1, false));
		if (pageMax.getRecords().isEmpty()) {
			return List.of();
		}
		Long maxContentId = pageMax.getRecords().get(0).getContentId();
		long random = RandomUtils.nextLong(minContentId, maxContentId);
		Page<CmsContent> page = this.contentService.lambdaQuery().eq(CmsContent::getSiteId, siteId)
				.likeRight(isCatalogNonNull, CmsContent::getCatalogAncestors, catalog.getAncestors())
				.ge(CmsContent::getContentId, random).orderByAsc(CmsContent::getContentId)
				.page(new Page<>(1, size, false));
		return page.getRecords();
	}

	private List<ContentDTO> findContentByIndex(long siteId, long catalogId, String keywords, int size) throws IOException {
		SearchResponse<ObjectNode> sr = esClient.search(s -> {
			s.index(ESContent.INDEX_NAME) // 索引
					.query(q ->
							q.bool(b -> {
								b.must(must -> must.term(tq -> tq.field("siteId").value(siteId)));
								if (catalogId > 0) {
									b.must(must -> must.term(tq -> tq.field("catalogId").value(catalogId)));
								}
								if (StringUtils.isNotEmpty(keywords)) {
									b.must(must -> must
											.multiMatch(match -> match
													.analyzer(SearchConsts.IKAnalyzeType_Smart)
													.fields("title^10", "fullText^1")
													.query(keywords)
											)
									);
								}
								return b;
							})
					);
			s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
			s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
			s.source(source -> source.filter(f -> f.excludes("fullText"))); // 过滤字段
			s.from(0).size(size);  // 分页，0开始
			return s;
		}, ObjectNode.class);
		return sr.hits().hits().stream().map(hit -> JacksonUtils.getObjectMapper().convertValue(hit.source(), ContentDTO.class)).toList();
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
