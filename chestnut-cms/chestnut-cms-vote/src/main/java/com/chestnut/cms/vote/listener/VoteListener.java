package com.chestnut.cms.vote.listener;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.chestnut.cms.vote.service.ICmsVoteService;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.listener.BeforeVoteAddEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VoteListener {

	private final ISiteService siteService;
	
	private final ICmsVoteService cmsVoteService;

	@EventListener
	public void beforeVoteAdd(BeforeVoteAddEvent event) {
		Vote vote = event.getVote();
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		if (Objects.nonNull(site)) {
			vote.setSource(this.cmsVoteService.getVoteSource(site.getSiteId()));
		}
	}
}
