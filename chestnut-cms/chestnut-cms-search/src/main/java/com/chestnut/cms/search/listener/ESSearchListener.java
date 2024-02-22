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
package com.chestnut.cms.search.listener;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.chestnut.cms.search.service.ContentIndexService;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.listener.event.AfterCatalogMoveEvent;
import com.chestnut.contentcore.listener.event.AfterContentDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterContentOfflineEvent;
import com.chestnut.contentcore.listener.event.AfterContentPublishEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ESSearchListener {

	private final ContentIndexService searchService;

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		try {
			this.searchService.deleteContentDoc(List.of(event.getContent().getContentEntity().getContentId()));
		} catch (ElasticsearchException | IOException e) {
			log.error("Delete es index document failed: {}", event.getContent().getContentEntity().getContentId(), e);
		}
	}

	@EventListener
	public void afterContentPublish(AfterContentPublishEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		IContent<?> content = event.getContent();
		this.searchService.createContentDoc(content);
	}

	@EventListener
	public void afterContentOfflineEvent(AfterContentOfflineEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		try {
			this.searchService.deleteContentDoc(List.of(event.getContent().getContentEntity().getContentId()));
		} catch (ElasticsearchException | IOException e) {
			log.error("Delete es index document failed: {}", event.getContent().getContentEntity().getContentId(), e);
		}
	}

	@EventListener
	public void afterCatalogMoveEvent(AfterCatalogMoveEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		try {
			this.searchService.rebuildCatalog(event.getFromCatalog(), true);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
