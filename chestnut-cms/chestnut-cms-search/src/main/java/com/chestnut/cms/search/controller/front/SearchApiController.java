/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.cms.search.controller.front;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.cms.search.vo.ESContentVO;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.search.SearchConsts;
import com.chestnut.search.service.ISearchLogService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cms/search")
public class SearchApiController extends BaseRestController {

	private final ICatalogService catalogService;

	private final ElasticsearchClient esClient;

	private final ISearchLogService logService;

	private final ContentDynamicDataService contentDynamicDataService;

	@GetMapping("/query")
	public R<?> selectDocumentList(
			@RequestParam(value = "sid") Long siteId,
			@RequestParam(value = "pp") String publishPipeCode,
			@RequestParam(value = "q") @Length(max = 50) String query,
			@RequestParam(value = "ot", required = false ,defaultValue = "false") Boolean onlyTitle,
			@RequestParam(value = "ct", required = false) String contentType,
			@RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
			@RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview) throws ElasticsearchException, IOException {
		int pageSize = 10;
		SearchResponse<ObjectNode> sr = esClient.search(s -> {
			s.index(ESContent.INDEX_NAME) // 索引
				.query(q ->
					q.bool(b -> {
						b.must(must -> must.term(tq -> tq.field("siteId").value(siteId)));
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
					h.fields("title", f -> f.preTags("<font color='red'>").postTags("</font>"))
						.fields("fullText", f -> f.preTags("<font color='red'>").postTags("</font>")));
			}
			s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
			s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
//			s.source(source -> source.filter(f -> f.excludes("fullText"))); // 过滤字段
			s.from((page - 1) * pageSize).size(pageSize);  // 分页，0开始
			return s;
		}, ObjectNode.class);
		List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
			ObjectNode source = hit.source();
			ESContentVO vo = JacksonUtils.getObjectMapper().convertValue(source, ESContentVO.class);
			vo.setHitScore(hit.score());
			vo.setPublishDateInstance(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
			vo.setCreateTimeInstance(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
			CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
			if (Objects.nonNull(catalog)) {
				vo.setCatalogName(catalog.getName());
			}
			hit.highlight().forEach((key, value) -> {
				try {
					if (key.equals("fullText")) {
						vo.setFullText(StringUtils.join(value.toArray(String[]::new)));
					} else if (key.equals("title")) {
						vo.setTitle(StringUtils.join(value.toArray(String[]::new)));
					}
					vo.setLink(InternalUrlUtils.getActualUrl(vo.getLink(), publishPipeCode, preview));
					vo.setLogo(InternalUrlUtils.getActualUrl(vo.getLogo(), publishPipeCode, preview));
				} catch (Exception ex) {
					log.warn("Search api row parse failed: ", ex);
				}
			});
			return vo;
		}).toList();
		List<String> contentIds = list.stream().map(c -> c.getContentId().toString()).toList();
		Map<Long, ContentDynamicDataVO> map = this.contentDynamicDataService.getContentDynamicDataList(contentIds)
				.stream().collect(Collectors.toMap(ContentDynamicDataVO::getContentId, i -> i));
		list.forEach(c -> {
			ContentDynamicDataVO cdd = map.get(c.getContentId());
			c.setViewCount(cdd.getViews());
			c.setFavoriteCount(cdd.getFavorites());
			c.setLikeCount(cdd.getLikes());
			c.setCommentCount(cdd.getComments());
		});
		// 记录搜索日志
		this.logService.addSearchLog("site:" + siteId, query, ServletUtils.getRequest());
		return this.bindDataTable(list, Objects.isNull(sr.hits().total()) ? 0 : sr.hits().total().value());
	}

	@GetMapping("/tag")
	public R<?> selectDocumentByTag(
			@RequestParam(value = "sid") Long siteId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "pp") String publishPipeCode,
			@RequestParam(value = "q", required = false) @Length(max = 200) String query,
			@RequestParam(value = "ct", required = false) String contentType,
			@RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
			@RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) Integer size,
			@RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview) throws ElasticsearchException, IOException {

		SearchResponse<ObjectNode> sr = esClient.search(s -> {
			s.index(ESContent.INDEX_NAME) // 索引
				.query(q ->
						q.bool(b -> {
							b.must(must -> must.term(tq -> tq.field("siteId").value(siteId)));
							if (IdUtils.validate(catalogId)) {
								b.must(must -> must.term(tq -> tq.field("catalogId").value(catalogId)));
							}
							if (StringUtils.isNotEmpty(contentType)) {
								b.must(must -> must.term(tq -> tq.field("contentType").value(contentType)));
							}
							if (StringUtils.isNotEmpty(query)) {
								String[] tags = query.split("\\s+");
								b.must(should -> {
									for (String tag : tags) {
										should.constantScore(cs ->
												cs.boost(1F).filter(f ->
														f.match(m ->
																m.field("tags").query(tag))));
									}
									return should;
								});
							}
							return b;
						})
				);
			s.sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)));
			s.sort(sort -> sort.field(f -> f.field("publishDate").order(SortOrder.Desc))); // 排序: _score:desc + publishDate:desc
			s.from((page - 1) * size).size(size);  // 分页，0开始
			return s;
		}, ObjectNode.class);
		List<ESContentVO> list = sr.hits().hits().stream().map(hit -> {
			ObjectNode source = hit.source();
			ESContentVO vo = JacksonUtils.getObjectMapper().convertValue(source, ESContentVO.class);
			vo.setHitScore(hit.score());
			vo.setPublishDateInstance(LocalDateTime.ofEpochSecond(vo.getPublishDate(), 0, ZoneOffset.UTC));
			vo.setCreateTimeInstance(LocalDateTime.ofEpochSecond(vo.getCreateTime(), 0, ZoneOffset.UTC));
			CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
			if (Objects.nonNull(catalog)) {
				vo.setCatalogName(catalog.getName());
			}
			vo.setLink(InternalUrlUtils.getActualUrl(vo.getLink(), publishPipeCode, preview));
			vo.setLogo(InternalUrlUtils.getActualUrl(vo.getLogo(), publishPipeCode, preview));
			return vo;
		}).toList();
		List<String> contentIds = list.stream().map(c -> c.getContentId().toString()).toList();
		Map<Long, ContentDynamicDataVO> map = this.contentDynamicDataService.getContentDynamicDataList(contentIds)
				.stream().collect(Collectors.toMap(ContentDynamicDataVO::getContentId, i -> i));
		list.forEach(c -> {
			ContentDynamicDataVO cdd = map.get(c.getContentId());
			c.setViewCount(cdd.getViews());
			c.setFavoriteCount(cdd.getFavorites());
			c.setLikeCount(cdd.getLikes());
			c.setCommentCount(cdd.getComments());
		});
		return this.bindDataTable(list, Objects.isNull(sr.hits().total()) ? 0 : sr.hits().total().value());
	}

	@GetMapping("/suggest")
	public R<?> getSuggestWords() {


		return R.ok();
	}
}
