package com.chestnut.cms.search.listener;

import java.io.IOException;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.chestnut.cms.search.service.ContentIndexService;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.listener.event.AfterCatalogMoveEvent;
import com.chestnut.contentcore.listener.event.AfterContentDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterContentOfflineEvent;
import com.chestnut.contentcore.listener.event.AfterContentPublishEvent;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		log.debug("Delete escontent index: " + event.getContent().getContentEntity().getTitle());
		try {
			this.searchService.deleteContentDoc(List.of(event.getContent().getContentEntity().getContentId()));
		} catch (ElasticsearchException | IOException e) {
			e.printStackTrace();
		}
	}

	@EventListener
	public void afterContentPublish(AfterContentPublishEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		log.debug("Create escontent index: " + event.getContent().getContentEntity().getTitle());
		IContent<?> content = event.getContent();
		this.searchService.createContentDoc(content);
	}

	@EventListener
	public void afterContentOfflineEvent(AfterContentOfflineEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		log.debug("Delete escontent index: " + event.getContent().getContentEntity().getTitle());
		try {
			this.searchService.deleteContentDoc(List.of(event.getContent().getContentEntity().getContentId()));
		} catch (ElasticsearchException | IOException e) {
			e.printStackTrace();
		}
	}

	@EventListener
	public void afterCatalogMoveEvent(AfterCatalogMoveEvent event) {
		if (!searchService.isElasticSearchAvailable()) {
			return;
		}
		log.debug("Rebuild escontent after catalog move: " + event.getFromCatalog().getName());
		this.searchService.rebuildCatalog(event.getFromCatalog(), true);
	}
}
