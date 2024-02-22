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
package com.chestnut.article.listener;

import com.chestnut.article.domain.vo.ArticleVO;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.AfterContentEditorInitEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleListener {

	private final CmsArticleDetailMapper articleMapper;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除文章数据
		try {
			long total = this.articleMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除文章详情备份数据：" + (i * pageSize) + "/" + total);
				this.articleMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除文章详情错误：" + e.getMessage());
			log.error("Delete article failed on site[{}] delete.", site.getSiteId(), e);
		}
	}

	@EventListener
	public void afterContentEditorInit(AfterContentEditorInitEvent event) {
		if (event.getContentVO() instanceof ArticleVO vo) {
			vo.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(vo.getContentHtml()));
		}
	}
}
