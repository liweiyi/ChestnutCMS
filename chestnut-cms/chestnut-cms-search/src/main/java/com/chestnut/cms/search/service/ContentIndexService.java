package com.chestnut.cms.search.service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.chestnut.common.utils.Assert;
import com.chestnut.exmodel.service.ExModelService;
import com.chestnut.search.SearchConsts;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.es.doc.ESContent;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.properties.EnableIndexProperty;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

	private void createIndex() throws IOException {
		// 创建索引
		Map<String, Property> properties = new HashMap<>();
//		properties.put("catalogAncestors", Property.of(fn -> fn.keyword(b -> b
//				.ignoreAbove(500) // 指定字符串字段的最大长度。超过该长度的字符串将被截断或忽略。
//		)));
//		properties.put("contentType", Property.of(fn -> fn.keyword(b -> b
//				.ignoreAbove(20)
//		)));
//		properties.put("logo", Property.of(fn -> fn.keyword(b -> b
//				.ignoreAbove(256)
//		)));
		properties.put("tags", Property.of(fn ->fn.keyword(b -> b.store(true))));
		properties.put("keywords", Property.of(fn ->fn.keyword(b -> b.store(true))));
		properties.put("title", Property.of(fn -> fn.text(b -> b
				.store(true) // 是否存储在索引中
				.analyzer(SearchConsts.IKAnalyzeType_Smart)
		)));
		properties.put("fullText", Property.of(fn -> fn.text(b -> b
				.analyzer(SearchConsts.IKAnalyzeType_Smart)
		)));
		CreateIndexResponse response = esClient.indices().create(fn -> fn
				.index(ESContent.INDEX_NAME)
				.mappings(mb -> mb.properties(properties)));
		Assert.isTrue(response.acknowledged(), () -> new RuntimeException("Create Index[cms_content] failed."));
	}

	public void recreateIndex(CmsSite site) throws IOException {
		boolean exists = esClient.indices().exists(fn -> fn.index(ESContent.INDEX_NAME)).value();
		if (!exists) {
			this.createIndex();
		} else {
			// 删除站点索引文档数据
			long total = this.contentService.lambdaQuery().eq(CmsContent::getSiteId, site.getSiteId()).count();
			long pageSize = 1000;
			for (int i = 0; i * pageSize < total; i++) {
				List<Long> contentIds = this.contentService.lambdaQuery()
						.select(List.of(CmsContent::getContentId))
						.eq(CmsContent::getSiteId, site.getSiteId())
						.page(new Page<>(i, pageSize, false))
						.getRecords().stream().map(CmsContent::getContentId).toList();
				deleteContentDoc(contentIds);
			}
		}
	}

	/**
	 * 创建/更新内容索引Document
	 */
	public void createContentDoc(IContent<?> content) {
		// 判断栏目/站点配置是否生成索引
		String enableIndex = EnableIndexProperty.getValue(content.getCatalog().getConfigProps(),
				content.getSite().getConfigProps());
		if (YesOrNo.isNo(enableIndex)) {
			return;
		}
		try {
			esClient.update(fn -> fn
					.index(ESContent.INDEX_NAME)
					.id(content.getContentEntity().getContentId().toString())
					.doc(newESContentDoc(content))
					.docAsUpsert(true), ESContent.class);
		} catch (ElasticsearchException | IOException e) {
			AsyncTaskManager.addErrMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	private void batchContentDoc(CmsSite site, CmsCatalog catalog, List<CmsContent> contents) {
		if (contents.isEmpty()) {
			return;
		}
		List<BulkOperation> bulkOperationList = new ArrayList<>(contents.size());
		for (CmsContent xContent : contents) {
			// 判断栏目/站点配置是否生成索引
			String enableIndex = EnableIndexProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
			if (YesOrNo.isYes(enableIndex)) {
				IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
				IContent<?> icontent = contentType.loadContent(xContent);
				BulkOperation bulkOperation = BulkOperation.of(b ->
								b.update(up -> up.index(ESContent.INDEX_NAME)
										.id(xContent.getContentId().toString())
										.action(action -> action.docAsUpsert(true).doc(newESContentDoc(icontent))))
//						b.create(co -> co.index(ESContent.INDEX_NAME)
//						.id(xContent.getContentId().toString()).document(newESContent(icontent)))
				);
				bulkOperationList.add(bulkOperation);
			}
		}
		if (bulkOperationList.isEmpty()) {
			return;
		}
		// 批量新增索引
		try {
			esClient.bulk(bulk -> bulk.operations(bulkOperationList));
		} catch (ElasticsearchException | IOException e) {
			AsyncTaskManager.addErrMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 删除内容索引
	 */
	public void deleteContentDoc(List<Long> contentIds) throws ElasticsearchException, IOException {
		List<BulkOperation> bulkOperationList = contentIds.stream().map(contentId -> BulkOperation
				.of(b -> b.delete(dq -> dq.index(ESContent.INDEX_NAME).id(contentId.toString())))).toList();
		this.esClient.bulk(bulk -> bulk.operations(bulkOperationList));
	}

	public void rebuildCatalog(CmsCatalog catalog, boolean includeChild) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		String enableIndex = EnableIndexProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		if (YesOrNo.isYes(enableIndex)) {
			LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
					.ne(CmsContent::getCopyType, ContentCopyType.Mapping)
					.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
					.eq(!includeChild, CmsContent::getCatalogId, catalog.getCatalogId())
					.likeRight(includeChild, CmsContent::getCatalogAncestors, catalog.getAncestors());
			long total = this.contentService.count(q);
			long pageSize = 200;
			for (int i = 0; i * pageSize < total; i++) {
				Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
				batchContentDoc(site, catalog, page.getRecords());
			}
		}
	}

	/**
	 * 重建指定站点所有内容索引
	 */
	public AsyncTask rebuildAll(CmsSite site) {
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				// 先重建索引
				recreateIndex(site);

				List<CmsCatalog> catalogs = catalogService.list();
				for (CmsCatalog catalog : catalogs) {
					LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
							.eq(CmsContent::getSiteId, site.getSiteId())
							.ne(CmsContent::getCopyType, ContentCopyType.Mapping)
							.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
							.eq(CmsContent::getCatalogId, catalog.getCatalogId());
					long total = contentService.count(q);
					int pageSize = 200;
					int count = 1;
					for (int i = 0; (long) i * pageSize < total; i++) {
						this.setProgressInfo((int) (count++ * 100 / total),
								"正在重建栏目【" + catalog.getName() + "】内容索引");
						Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
						batchContentDoc(site, catalog, page.getRecords());
						AsyncTaskManager.checkInterrupt(); // 允许中断
					}
				}
				this.setProgressInfo(100, "重建全站索引完成");
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
	public ESContent getContentDocDetail(Long contentId) throws ElasticsearchException, IOException {
		GetResponse<ESContent> res = this.esClient.get(qb -> qb.index(ESContent.INDEX_NAME).id(contentId.toString()),
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
			data.put(fd.getFieldName(), fd.getValue());
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
			boolean exists = esClient.indices().exists(fn -> fn.index(ESContent.INDEX_NAME)).value();
			if (!exists) {
				this.createIndex(); // 创建内容索引库
			}
		} else {
			log.warn("ES service not available!");
		}
	}
}
