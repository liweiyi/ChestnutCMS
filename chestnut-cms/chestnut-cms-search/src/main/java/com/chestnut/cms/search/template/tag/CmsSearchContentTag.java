package com.chestnut.cms.search.template.tag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.cms.search.vo.ESContentVO;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.search.SearchConsts;
import com.fasterxml.jackson.databind.node.ObjectNode;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsSearchContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_search_content";

	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final ElasticsearchClient esClient;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("query", true, TagAttrDataType.STRING, "检索词"));
		tagAttrs.add(new TagAttr("contenttype", false, TagAttrDataType.STRING, "内容类型"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		String query = MapUtils.getString(attrs, "query");
		if (StringUtils.isEmpty(query)) {
			throw new TemplateException("Tag attr `query` cannot be empty.", env);
		}
		String[] keywords = StringUtils.split(query, ",");
		String contentType = MapUtils.getString(attrs, "contenttype");
		try {
			SearchResponse<ObjectNode> sr = esClient.search(s -> {
				s.index(ESContent.INDEX_NAME) // 索引
					.query(q ->
						q.bool(b -> {
							b.must(must -> must.term(tq -> tq.field("siteId").value(siteId)));
							if (StringUtils.isNotEmpty(contentType)) {
								b.must(must -> must.term(tq -> tq.field("contentType").value(contentType)));
							}
							b.must(should -> {
								for (String keyword : keywords) {
									should.constantScore(cs ->
										cs.boost(1F).filter(f ->
											f.match(m ->
												m.field("tags").query(keyword))));
								}
								return should;
							});
							return b;
						})
					);
				s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
				s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
				s.source(source -> source.filter(f -> f.excludes("fullText"))); // 过滤字段
				if (page) {
					s.from((pageIndex - 1) * size).size(size);  // 分页
				} else {
					s.from(0).size(size);  // 分页，0开始
				}
				return s;
			}, ObjectNode.class);
			List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
				ESContentVO vo = JacksonUtils.getObjectMapper().convertValue(hit.source(), ESContentVO.class);
				vo.setHitScore(hit.score());
				vo.setPublishDateInstance(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
				vo.setCreateTimeInstance(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
				return vo;
			}).toList();
			return TagPageData.of(list, page ? sr.hits().total().value() : list.size());
		} catch (IOException e) {
			throw new TemplateException(e, env);
		}
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