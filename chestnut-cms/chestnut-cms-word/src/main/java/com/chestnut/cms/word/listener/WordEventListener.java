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
package com.chestnut.cms.word.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.article.ArticleContentType;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.cms.word.properties.HotWordGroupsProperty;
import com.chestnut.cms.word.properties.HotWordMaxReplaceCountProperty;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.listener.event.BeforeContentSaveEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.word.WordConstants;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordEventListener {
	
	private final ISensitiveWordService sensitiveWordService;
	
	private final IHotWordService hotWordService;

	private final IHotWordGroupService hotWordGroupService;

	private final ITagWordService tagWordService;

	private final ITagWordGroupService tagWordGroupService;
	
	private final IErrorProneWordService errorProneWordService;
	
	private final ISiteService siteService;

	@EventListener
	public void beforeContentSave(BeforeContentSaveEvent event) {
		IContent<?> content = event.getContent();
		if (ArticleContentType.ID.equals(content.getContentType()) && !content.getContentEntity().isLinkContent()
				&& !ContentCopyType.isMapping(content.getContentEntity().getCopyType())) {
			CmsArticleDetail articleDetail = (CmsArticleDetail) content.getExtendEntity();
			String contentHtml = articleDetail.getContentHtml();
			// 敏感词处理
			contentHtml = sensitiveWordService.replaceSensitiveWords(contentHtml, null);
			// 易错词处理
			contentHtml = errorProneWordService.replaceErrorProneWords(contentHtml);
			// 热词处理
			CmsSite site = siteService.getSite(content.getCatalog().getSiteId());
			String[] groupCodes = HotWordGroupsProperty.getHotWordGroupCodes(content.getCatalog().getConfigProps(), site.getConfigProps());
			if (Objects.nonNull(groupCodes) && groupCodes.length > 0) {
                int maxReplaceCount = HotWordMaxReplaceCountProperty.getHotWordMaxReplaceCount(content.getCatalog().getConfigProps(), site.getConfigProps());
                contentHtml = hotWordService.replaceHotWords(contentHtml, site.getSiteId().toString(), groupCodes, maxReplaceCount, null, null);
			} else {
                // 移除热词
                contentHtml = WordConstants.HOT_WORD_LINK_PATTERN.matcher(contentHtml).replaceAll("$1");
            }
			articleDetail.setContentHtml(contentHtml);
		}
	}

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		long pageSize = 500;
		long total;
		try {
			// 删除热词分组数据
			total = this.hotWordGroupService
					.count(new LambdaQueryWrapper<HotWordGroup>().eq(HotWordGroup::getOwner, site.getSiteId()));
			long lastId = 0;
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删热词分组数据：" + (i * pageSize) + "/" + total);
				Page<HotWordGroup> groups = this.hotWordGroupService.lambdaQuery()
						.select(HotWordGroup::getGroupId)
						.eq(HotWordGroup::getOwner, site.getSiteId())
						.gt(HotWordGroup::getGroupId, lastId)
						.orderByAsc(HotWordGroup::getGroupId)
						.page(Page.of(1, pageSize, false));
				if (!groups.getRecords().isEmpty()) {
					List<Long> groupIds = groups.getRecords().stream().map(HotWordGroup::getGroupId).toList();
					this.hotWordGroupService.removeBatchByIds(groupIds);
					lastId = groupIds.get(groupIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除热词分组数据错误：" + e.getMessage());
			log.error("Delete hot word group failed on site[{}] delete", site.getSiteId());
		}
		try {
			// 删除热词数据
			total = this.hotWordService
					.count(new LambdaQueryWrapper<HotWord>().eq(HotWord::getOwner, site.getSiteId()));
			long lastId = 0;
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删热词数据：" + (i * pageSize) + "/" + total);
				Page<HotWord> hotWords = this.hotWordService.lambdaQuery()
						.select(HotWord::getWordId)
						.eq(HotWord::getOwner, site.getSiteId())
						.gt(HotWord::getWordId, lastId)
						.orderByAsc(HotWord::getWordId)
						.page(Page.of(1, pageSize, false));
				if (!hotWords.getRecords().isEmpty()) {
					List<Long> wordIds = hotWords.getRecords().stream().map(HotWord::getWordId).toList();
					this.hotWordService.removeBatchByIds(wordIds);
					lastId = wordIds.get(wordIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除热词数据错误：" + e.getMessage());
			log.error("Delete hot word failed on site[{}] delete", site.getSiteId());
		}
		try {
			// 删除TAG词分组数据
			total = this.tagWordGroupService
					.count(new LambdaQueryWrapper<TagWordGroup>().eq(TagWordGroup::getOwner, site.getSiteId()));
			long lastId = 0;
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除TAG词分组数据：" + (i * pageSize) + "/" + total);
				Page<TagWordGroup> groups = this.tagWordGroupService.lambdaQuery()
						.select(TagWordGroup::getGroupId)
						.eq(TagWordGroup::getOwner, site.getSiteId())
						.gt(TagWordGroup::getGroupId, lastId)
						.orderByAsc(TagWordGroup::getGroupId)
						.page(Page.of(1, pageSize, false));
				if (!groups.getRecords().isEmpty()) {
					List<Long> groupIds = groups.getRecords().stream().map(TagWordGroup::getGroupId).toList();
					this.tagWordGroupService.removeBatchByIds(groupIds);
					lastId = groupIds.get(groupIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除TAG词分组数据错误：" + e.getMessage());
			log.error("Delete tag word group failed on site[{}] delete", site.getSiteId());
		}
		try {
			// 删除TAG词数据
			total = this.tagWordService
					.count(new LambdaQueryWrapper<TagWord>().eq(TagWord::getOwner, site.getSiteId()));
			long lastId = 0;
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除TAG词数据：" + (i * pageSize) + "/" + total);
				Page<TagWord> tagWords = this.tagWordService.lambdaQuery()
						.select(TagWord::getWordId)
						.eq(TagWord::getOwner, site.getSiteId())
						.gt(TagWord::getWordId, lastId)
						.orderByAsc(TagWord::getWordId)
						.page(Page.of(1, pageSize, false));
				if (!tagWords.getRecords().isEmpty()) {
					List<Long> wordIds = tagWords.getRecords().stream().map(TagWord::getWordId).toList();
					this.tagWordService.removeBatchByIds(wordIds);
					lastId = wordIds.get(wordIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除TAG词数据错误：" + e.getMessage());
			log.error("Delete tag word failed on site[{}] delete", site.getSiteId());
		}
	}
}

