/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SiteDashboardVO {

	private Long siteId;
	
	private String name;
	
	private List<PublishPipeProps> urls;

	public static SiteDashboardVO create(CmsSite site, List<CmsPublishPipe> publishPipes) {
		SiteDashboardVO vo = new SiteDashboardVO();
		vo.setSiteId(site.getSiteId());
		vo.setName(site.getName());
		List<PublishPipeProps> urls = publishPipes.stream().map(pp -> PublishPipeProps.newInstance(
				pp.getCode(), pp.getName(), Map.of("url", site.getUrl(pp.getCode()))
		)).toList();
		vo.setUrls(urls);
		return vo;
	}
}
