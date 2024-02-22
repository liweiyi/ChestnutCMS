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
package com.chestnut.advertisement.pojo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementDTO extends BaseDTO {
    
	/**
	 * 广告ID
	 */
    @NotNull
	private Long advertisementId;

    /**
     * 广告位ID（等同页面部件ID）
     */
    @NotNull
	private Long adSpaceId;

    /**
     * 广告类型
     */
    @NotNull
    private String type;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * 权重
     */
    @NotNull
    private Integer weight;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 上线时间
     */
    @NotNull
    private LocalDateTime onlineDate;

    /**
     * 下线时间
     */
    @NotNull
    private LocalDateTime offlineDate;
    
    /**
     * 跳转链接
     */
    @NotNull
    private String redirectUrl;
    
    /**
     * 素材链接
     */
    @NotNull
    private String resourcePath;
}
