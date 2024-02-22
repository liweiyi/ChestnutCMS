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
package com.chestnut.advertisement.pojo.vo;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 广告数据VO
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
public class AdvertisementVO {

	/**
	 * 广告ID
	 */
    private Long advertisementId;
	
    /**
     * 所属广告位ID
     */
	private Long adSpaceId;
	
	/**
	 * 类型
	 */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 状态
     */
    private String state;
    
    /**
     * 上线时间
     */
    private LocalDateTime onlineDate;

    /**
     * 下线时间
     */
    private LocalDateTime offlineDate;

    /**
     * 跳转链接
     */
    private String redirectUrl;

    /**
     * 跳转链接（可设置为中转地址）
     */
    private String link;
    
    /**
     * 素材链接
     */
    private String resourcePath;
    
    /**
     * 素材真实地址
     */
    private String resourceSrc;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    public AdvertisementVO(CmsAdvertisement ad) {
    	this.advertisementId = ad.getAdvertisementId();
    	this.adSpaceId = ad.getAdSpaceId();
    	this.type = ad.getType();
    	this.name = ad.getName();
    	this.weight = ad.getWeight();
    	this.keywords = ad.getKeywords();
    	this.state = ad.getState();
    	this.onlineDate = ad.getOnlineDate();
    	this.offlineDate = ad.getOfflineDate();
    	this.redirectUrl = ad.getRedirectUrl();
    	this.resourcePath = ad.getResourcePath();
    	this.createBy = ad.getCreateBy();
    	this.createTime = ad.getCreateTime();
    }
    
    public AdvertisementVO dealPreviewResourcePath() {
    	return dealResourcePath(null, true);
    }
    
    public AdvertisementVO dealResourcePath(String publishPipeCode, boolean isPreview) {
    	if (StringUtils.isNotEmpty(this.getResourcePath())) {
    		this.setResourceSrc(InternalUrlUtils.getActualUrl(this.getResourcePath(), publishPipeCode, isPreview));
    	}
    	return this;
    }
}
