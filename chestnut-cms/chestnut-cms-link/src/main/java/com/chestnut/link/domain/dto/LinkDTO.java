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
package com.chestnut.link.domain.dto;

import org.springframework.beans.BeanUtils;

import com.chestnut.link.domain.CmsLink;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkDTO {

    private Long linkId;

    private Long siteId;

    @NotNull
    private Long groupId;

    @NotNull
    private String name;

    @NotNull
    private String url;

    private String logo;
    
    private Long sortFlag;

    private String remark;
    
	public static LinkDTO newInstance(CmsLink link) {
		LinkDTO dto = new LinkDTO();
    	BeanUtils.copyProperties(link, dto);
		return dto;
	}
}
