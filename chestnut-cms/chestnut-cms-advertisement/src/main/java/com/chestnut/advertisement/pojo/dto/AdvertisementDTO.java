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
package com.chestnut.advertisement.pojo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@XComment("{API.DOC.CMS.ADVERTISEMENT.DTO}")
@Getter
@Setter
public class AdvertisementDTO extends BaseDTO {

    @NotNull
    @XComment("{CMS.AD.ID}")
	private Long advertisementId;

    @NotNull
    @XComment("{CMS.AD.SPACE_ID}")
	private Long adSpaceId;

    @NotNull
    @XComment("{CMS.AD.TYPE}")
    private String type;

    @NotNull
    @XComment("{CMS.AD.NAME}")
    private String name;

    @NotNull
    @XComment("{CMS.AD.WEIGHT}")
    private Integer weight;

    @XComment("{CMS.AD.KEYWORDS}")
    private String keywords;

    @NotNull
    @XComment("{CMS.AD.ONLINE_DATE}")
    private LocalDateTime onlineDate;

    @NotNull
    @XComment("{CMS.AD.OFFLINE_DATE}")
    private LocalDateTime offlineDate;

    @NotNull
    @XComment("{CMS.AD.REDIRECT_URL}")
    private String redirectUrl;

    @NotNull
    @XComment("{CMS.AD.RESOURCE_PATH}")
    private String resourcePath;

    @Length(max = 255)
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
}
