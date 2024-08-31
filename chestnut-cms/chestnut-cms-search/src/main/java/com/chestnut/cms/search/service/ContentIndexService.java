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
package com.chestnut.cms.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.CmsSearchConstants;
import com.chestnut.cms.search.CmsSearchErrorCode;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.cms.search.fixed.config.SearchAnalyzeType;
import com.chestnut.cms.search.properties.EnableIndexProperty;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.exmodel.service.ExModelService;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContentIndexService implements CommandLineRunner {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final AsyncTaskManager asyncTaskManager;

	private final ElasticsearchClient esClient;

	private final ExModelService extendModelService;

	public void createIndex(CmsSite site) {
		String indexName = CmsSearchConstants.indexName(site.getSiteId().toString());
		try {
			boolean exists = esClient.indices().exists(fn -> fn.index(indexName)).value();
			if (exists) {
				return;
			}
			String analyzeType = SearchAnalyzeType.getValue();
			// 创建索引
			TypeMapping typeMapping = new TypeMapping.Builder()
				.properties("title", fn -> fn.text(b -> b
					.analyzer(analyzeType)
					.fields("suggest", s -> s
						.completion(c -> c
							.analyzer(analyzeType) // The index analyzer to use, defaults to simple.
							.searchAnalyzer(analyzeType) // The search analyzer to use, defaults to value of analyzer.
//							.preserveSeparators(true) // 默认true，设置成false会忽略空格进行匹配
//							.preservePositionIncrements(true) // 默认true，设置成false会忽略停用词
							.maxInputLength(50)
					))
				))
				.properties("fullText", fn -> fn.text(b -> b
					.analyzer(analyzeType)
				))
				.properties("keywords", fn -> fn
					.keyword(b -> b.index(false)
//						.ignoreAbove(500) // keywords可通过设置ignoreAbove指定字段被索引的最大长度（默认值：256）。超过该长度的字符串将不会被索引，但依然会存储。
					)
				)
				.properties("tags", fn -> fn.keyword(b -> b.index(false)))
				.properties("contentType", fn -> fn.keyword(b -> b))
				.properties("catalogAncestors", fn -> fn.keyword(b -> b))
				.properties("logo", fn -> fn.keyword(b -> b.index(false)))
				.properties("author", fn -> fn.keyword(b -> b))
				.properties("editor", fn -> fn.keyword(b -> b))
				.properties("status", fn -> fn.keyword(b -> b))
				.properties("publishDate", fn -> fn.long_(l -> l))
				.build();
			CreateIndexResponse response = esClient.indices().create(fn -> fn
					.index(indexName)
					.mappings(typeMapping));
			Assert.isTrue(response.acknowledged(), CmsSearchErrorCode.CREATE_INDEX_ERR::exception);
		} catch (IOException e) {
			log.error("Create elasticsearch index failed: " + indexName, e);
		}
	}

	/**
	 * 删除站点索引库
	 */
	public void deleteIndex(CmsSite site) {
		String indexName = CmsSearchConstants.indexName(site.getSiteId().toString());
		try {
			boolean exists = esClient.indices().exists(fn -> fn.index(indexName)).value();
			if (exists) {
				return;
			}
			esClient.indices().delete(fn -> fn.index(indexName));
		} catch (IOException e) {
			log.error("Delete elasticsearch index failed: " + indexName, e);
		}
	}

	public void deleteContentIndices(CmsSite site) throws IOException {
		createIndex(site);
		// 删除站点索引文档数据
		long total = this.contentService.dao().lambdaQuery().eq(CmsContent::getSiteId, site.getSiteId()).count();
		long pageSize = 1000;
		for (int i = 0; i * pageSize < total; i++) {
			List<Long> contentIds = this.contentService.dao().lambdaQuery()
					.select(List.of(CmsContent::getContentId))
					.eq(CmsContent::getSiteId, site.getSiteId())
					.page(new Page<>(i, pageSize, false))
					.getRecords().stream().map(CmsContent::getContentId).toList();
			deleteContentDoc(site.getSiteId(), contentIds);
		}
	}

	/**
	 * 创建/更新内容索引Document
	 */
	public void createContentDoc(IContent<?> content) {
		CmsSite site = siteService.getSite(content.getSiteId());
		createIndex(site);
		// 判断栏目/站点配置是否生成索引
		String enableIndex = EnableIndexProperty.getValue(content.getCatalog().getConfigProps(),
				content.getSite().getConfigProps());
		if (YesOrNo.isNo(enableIndex)) {
			return;
		}
		try {
			esClient.update(fn -> fn
					.index(CmsSearchConstants.indexName(content.getSiteId().toString()))
					.id(content.getContentEntity().getContentId().toString())
					.doc(newESContentDoc(content))
					.docAsUpsert(true), ESContent.class);
		} catch (ElasticsearchException | IOException e) {
			AsyncTaskManager.addErrMessage(e.getMessage());
			log.error("Update es index document failed", e);
		}
	}

	private void batchContentDoc(CmsSite site, CmsCatalog catalog, List<CmsContent> contents) throws IOException {
		if (contents.isEmpty()) {
			return;
		}
		List<BulkOperation> bulkOperationList = new ArrayList<>(contents.size());
		for (CmsContent xContent : contents) {
			// 判断栏目/站点配置是否生成索引
			String enableIndex = EnableIndexProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
			if (YesOrNo.isYes(enableIndex)) {
				try {
					IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
					IContent<?> icontent = contentType.loadContent(xContent);
					BulkOperation bulkOperation = BulkOperation.of(b ->
									b.update(up -> up.index(CmsSearchConstants.indexName(xContent.getSiteId().toString()))
											.id(xContent.getContentId().toString())
											.action(action -> action.docAsUpsert(true).doc(newESContentDoc(icontent))))
					);
					bulkOperationList.add(bulkOperation);
				} catch (Exception e) {
					log.error("Generate es content instance fail.", e);
					AsyncTaskManager.addErrMessage(e.getMessage());
				}
			}
		}
		if (!bulkOperationList.isEmpty()) {
			esClient.bulk(bulk -> bulk.operations(bulkOperationList));
		}
	}

	/**
	 * 删除内容索引
	 */
	public void deleteContentDoc(Long siteId, List<Long> contentIds) throws ElasticsearchException, IOException {
		CmsSite site = siteService.getSite(siteId);
		createIndex(site);
		List<BulkOperation> bulkOperationList = contentIds.stream().map(contentId -> BulkOperation
				.of(b -> b.delete(dq -> dq
						.index(CmsSearchConstants.indexName(siteId.toString()))
						.id(contentId.toString())
				))).toList();
		this.esClient.bulk(bulk -> bulk.operations(bulkOperationList));
	}

	public void rebuildCatalog(CmsCatalog catalog, boolean includeChild) throws InterruptedException {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		String enableIndex = EnableIndexProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		if (YesOrNo.isYes(enableIndex)) {
			LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
					.ne(CmsContent::getCopyType, ContentCopyType.Mapping)
					.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
					.ne(CmsContent::getLinkFlag, YesOrNo.YES)
					.eq(!includeChild, CmsContent::getCatalogId, catalog.getCatalogId())
					.likeRight(includeChild, CmsContent::getCatalogAncestors, catalog.getAncestors());
			long total = this.contentService.dao().count(q);
			long pageSize = 200;
			int count = 1;
			for (int i = 0; i * pageSize < total; i++) {
                try {
					AsyncTaskManager.setTaskProgressInfo((int) (count++ * 100 / total),
							"正在重建栏目【" + catalog.getName() + "】内容索引");
					AsyncTaskManager.checkInterrupt(); // 允许中断
					Page<CmsContent> page = contentService.dao().page(new Page<>(i, pageSize, false), q);
                    batchContentDoc(site, catalog, page.getRecords());
                } catch (IOException e) {
                    log.error("Create es documents fail.", e);
					AsyncTaskManager.addErrMessage(e.getMessage());
                }
			}
		}
	}

	/**
	 * 重建指定站点所有内容索引
	 */
	public AsyncTask rebuildAll(CmsSite site) {
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() {
				try {
					String indexName = CmsSearchConstants.indexName(site.getSiteId().toString());
					boolean exists = esClient.indices().exists(fn -> fn.index(indexName)).value();
					if (exists) {
						// 先删除内容索引文档
						deleteContentIndices(site);

						esClient.indices().delete(fn ->fn.index(indexName));
						while(exists) {
							exists = esClient.indices().exists(fn -> fn.index(indexName)).value();
							if (exists) {
								Thread.sleep(2000);
							}
						}
					}
					createIndex(site);

					List<CmsCatalog> catalogs = catalogService.lambdaQuery()
							.eq(CmsCatalog::getSiteId, site.getSiteId()).list();
					for (CmsCatalog catalog : catalogs) {
						rebuildCatalog(catalog, false);
					}
					this.setProgressInfo(100, "重建全站索引完成");
				} catch (Exception e) {
					log.error("RebuildAllContentIndex failed.", e);
					addErrorMessage(e.getMessage());
				}
            }
		};
		asyncTask.setTaskId("RebuildAllContentIndex");
		asyncTask.setType("ContentCore");
		asyncTask.setInterruptible(true);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	/**
	 * 获取指定内容索引详情
	 *
	 * @param contentId 内容ID
	 * @return 索引Document详情
	 */
	public ESContent getContentDocDetail(Long siteId, Long contentId) throws ElasticsearchException, IOException {
		CmsSite site = siteService.getSite(siteId);
		createIndex(site);
		GetResponse<ESContent> res = this.esClient.get(qb -> qb
						.index(CmsSearchConstants.indexName(siteId.toString()))
						.id(contentId.toString()),
				ESContent.class);
		return res.source();
	}

	private Map<String, Object> newESContentDoc(IContent<?> content) {
		Map<String, Object> data = new HashMap<>();
		data.put("contentId", content.getContentEntity().getContentId());
		data.put("contentType", content.getContentEntity().getContentType());
		data.put("siteId", content.getSiteId());
		data.put("catalogId", content.getCatalogId());
		data.put("catalogAncestors", content.getContentEntity().getCatalogAncestors());
		data.put("topCatalog", Long.valueOf(content.getCatalog().getAncestors().split(":")[0]));
		data.put("author", content.getContentEntity().getAuthor());
		data.put("editor", content.getContentEntity().getEditor());
		data.put("keywords", content.getContentEntity().getKeywords());
		data.put("tags", content.getContentEntity().getTags());
		data.put("createTime", content.getContentEntity().getCreateTime().toEpochSecond(ZoneOffset.UTC));
		data.put("logo", content.getContentEntity().getLogo());
		data.put("status", content.getContentEntity().getStatus());
		data.put("publishDate", content.getContentEntity().getPublishDate().toEpochSecond(ZoneOffset.UTC));
		data.put("link", InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, content.getContentEntity().getContentId()));
		data.put("title", content.getContentEntity().getTitle());
		data.put("summary", content.getContentEntity().getSummary());
		data.put("fullText", content.getFullText());
		// 扩展模型数据
		this.extendModelService.getModelData(content.getContentEntity()).forEach(fd -> {
			if (fd.getValue() instanceof LocalDateTime date) {
				data.put(fd.getFieldName(), date.toInstant(ZoneOffset.UTC).toEpochMilli());
			} else if (fd.getValue() instanceof LocalDate date) {
				data.put(fd.getFieldName(), date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
			} else if (fd.getValue() instanceof Instant date) {
				data.put(fd.getFieldName(), date.toEpochMilli());
			} else {
				data.put(fd.getFieldName(), fd.getValue());
			}
		});
		return data;
	}

	public boolean isElasticSearchAvailable() {
		try {
			return esClient.ping().value();
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void run(String... args) throws Exception {
		if (isElasticSearchAvailable()) {
            // 创建内容索引库
            this.siteService.lambdaQuery()
					.select(CmsSite::getSiteId)
					.list()
					.forEach(this::createIndex);
		} else {
			log.warn("ES service not available!");
		}
	}
}
