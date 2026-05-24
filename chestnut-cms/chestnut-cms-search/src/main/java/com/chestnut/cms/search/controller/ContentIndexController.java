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
package com.chestnut.cms.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.chestnut.cms.search.CmsSearchConstants;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.cms.search.permission.CmsSearchPriv;
import com.chestnut.cms.search.service.ContentIndexService;
import com.chestnut.cms.search.vo.ESContentVO;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;


import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.search.SearchConsts;
import com.chestnut.search.exception.SearchErrorCode;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import com.chestnut.xmodel.core.IMetaModelType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.jsoup.safety.Safelist;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX_MODULE}")
@Priv(type = AdminUserType.TYPE, value = CmsSearchPriv.ContentIndexView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/search")
public class ContentIndexController extends CmsRestController {

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ContentIndexService searchService;

	private final ElasticsearchClient esClient;

	private void checkElasticSearchEnabled() {
		Assert.isTrue(this.searchService.isElasticSearchAvailable(), SearchErrorCode.ESConnectFail::exception);
	}

	@GetMapping("/checkExists")
	public R<Boolean> checkSiteIndexExists() throws IOException {
		this.checkElasticSearchEnabled();
		CmsSite site = this.getCurrentSite();
		String indexName = CmsSearchConstants.indexName(site.getSiteId().toString());
		BooleanResponse response = esClient.indices().exists(fn -> fn.index(indexName));
		return R.ok(response.value());
	}

	@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.GET_LIST}")
	@GetMapping("/contents")
	public R<TableData<ESContentVO>> selectDocumentList(@RequestParam(value = "query", required = false) @Length(max = 200) String query,
								   @RequestParam(value = "onlyTitle", required = false ,defaultValue = "false") Boolean onlyTitle,
								   @RequestParam(value = "contentType", required = false) @Length(max = 20) String contentType) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.getCurrentSite();
		String indexName = CmsSearchConstants.indexName(site.getSiteId().toString());
		SearchResponse<ObjectNode> sr = esClient.search(s -> {
			s.index(indexName) // 索引
					.query(q ->
							q.bool(b -> {
								if (StringUtils.isNotEmpty(contentType)) {
									b.must(must -> must.term(tq -> tq.field("contentType").value(contentType)));
								}
								if (StringUtils.isNotEmpty(query)) {
									if (onlyTitle) {
										b.must(must -> must
												.match(match -> match
														.analyzer(SearchConsts.IKAnalyzeType_Smart)
														.field("title")
														.query(query)
												)
										);
									} else {
										b.must(must -> must
												.multiMatch(match -> match
														.analyzer(SearchConsts.IKAnalyzeType_Smart)
														.fields("title^10", "fullText^1")
														.query(query)
												)
										);
									}
								}
								return b;
							})
					);
			if (StringUtils.isNotEmpty(query)) {
				s.highlight(h ->
						h.fields("title", f -> f.preTags("<em color='red'>").postTags("</em>"))
								.fields("fullText", f -> f.preTags("<em color='red'>").postTags("</em>")));
			}
			s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
			s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
//			s.source(source -> source.filter(f -> f.excludes("fullText"))); // 过滤字段
			s.from((pr.getPageNumber() - 1) * pr.getPageSize()).size(pr.getPageSize());  // 分页
			return s;
		}, ObjectNode.class);
		List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
			ObjectNode source = hit.source();
			ESContentVO vo = JacksonUtils.getObjectMapper().convertValue(source, ESContentVO.class);
			source.fieldNames().forEachRemaining(fieldName -> {
				if (fieldName.startsWith(IMetaModelType.DATA_FIELD_PREFIX)) {
					vo.getExtendData().put(fieldName, source.get(fieldName).asText());
				}
			});
			vo.setHitScore(hit.score());
			vo.setPublishDateInstance(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
			vo.setCreateTimeInstance(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
			CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
			if (Objects.nonNull(catalog)) {
				String catalogName = Stream.of(catalog.getAncestors().split(CatalogUtils.ANCESTORS_SPLITER)).map(cid -> {
					CmsCatalog parent = this.catalogService.getCatalog(Long.valueOf(cid));
					return Objects.nonNull(parent) ? parent.getName() : "[Unknown]";
				}).collect(Collectors.joining(" > "));
				vo.setCatalogName(catalogName);
			}
			hit.highlight().forEach((key, value) -> {
                if (key.equals("fullText")) {
                    vo.setFullText(StringUtils.join(value.toArray(String[]::new)));
                } else if (key.equals("title")) {
                    vo.setTitle(StringUtils.join(value.toArray(String[]::new)));
                }
            });
			vo.setTitle(HtmlUtils.clean(vo.getTitle(), Safelist.simpleText()));
			return vo;
		}).toList();
		return this.bindDataTable(list, sr.hits().total().value());
	}

	@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.GET_DETAIL}")
	@GetMapping("/content/{contentId}")
	public R<ESContent> selectDocumentDetail(@PathVariable(value = "contentId") @LongId @XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.CONTENT_ID}") Long contentId) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		CmsSite site = this.getCurrentSite();
		ESContent source = this.searchService.getContentDocDetail(site.getSiteId(), contentId);
		return R.ok(source);
	}

	@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.DELETE}")
	@Log(title = "删除索引", businessType = BusinessType.DELETE)
	@PostMapping("/contents/delete")
	public R<Void> deleteDocuments(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.CONTENT_IDS}") List<Long> contentIds) throws ElasticsearchException, IOException {
		this.checkElasticSearchEnabled();
		CmsSite site = this.getCurrentSite();
		this.searchService.deleteContentDoc(site.getSiteId(), contentIds);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.BUILD}")
	@Log(title = "重建内容索引", businessType = BusinessType.UPDATE)
	@PostMapping("/build/{contentId}")
	public R<Void> buildContentIndex(@PathVariable("contentId") @LongId @XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.CONTENT_ID}") Long contentId) {
		this.checkElasticSearchEnabled();
		CmsContent content = this.contentService.dao().getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

		IContentType ct = ContentCoreUtils.getContentType(content.getContentType());
		IContent<?> icontent = ct.loadContent(content);
		this.searchService.createContentDoc(icontent);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.SEARCH.CONTENT_INDEX.REBUILD_ALL}")
	@Log(title = "重建全站索引", businessType = BusinessType.UPDATE)
	@PostMapping("/rebuild")
	public R<String> rebuildAllIndex() {
		this.checkElasticSearchEnabled();
		CmsSite site = this.getCurrentSite();
		AsyncTask task = this.searchService.rebuildAll(site);
		return R.ok(task.getTaskId());
	}
}
