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
