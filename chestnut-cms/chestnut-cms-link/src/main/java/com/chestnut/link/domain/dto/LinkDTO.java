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
package com.chestnut.link.domain.dto;

import com.chestnut.common.annotation.XComment;
import org.springframework.beans.BeanUtils;

import com.chestnut.link.domain.CmsLink;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XComment("{API.DOC.CMS.LINK.DTO}")
public class LinkDTO {

	@XComment("{CMS.LINK.ID}")
    private Long linkId;

	@XComment("{CMS.LINK.SITE_ID}")
    private Long siteId;

    @NotNull
	@XComment("{CMS.LINK.GROUP_ID}")
    private Long groupId;

    @NotNull
	@XComment("{CMS.LINK.NAME}")
    private String name;

    @NotNull
	@XComment("{CMS.LINK.URL}")
    private String url;

	@XComment("{CMS.LINK.LOGO}")
    private String logo;

	@XComment("{API.DOC.CMS.LINK.SORT_FLAG}")
    private Long sortFlag;

	@XComment("{API.DOC.CMS.LINK.REMARK}")
    private String remark;
    
	public static LinkDTO newInstance(CmsLink link) {
		LinkDTO dto = new LinkDTO();
    	BeanUtils.copyProperties(link, dto);
		return dto;
	}
}
