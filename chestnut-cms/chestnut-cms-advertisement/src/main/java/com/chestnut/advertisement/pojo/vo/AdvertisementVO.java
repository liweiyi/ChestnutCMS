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
package com.chestnut.advertisement.pojo.vo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @XComment("{CMS.AD.ID}")
    private Long advertisementId;
	
    @XComment("{CMS.AD.SPACE_ID}")
	private Long adSpaceId;
	
    @XComment("{CMS.AD.TYPE}")
    private String type;

    @XComment("{CMS.AD.NAME}")
    private String name;

    @XComment("{CMS.AD.WEIGHT}")
    private Integer weight;

    @XComment("{CMS.AD.KEYWORDS}")
    private String keywords;

    @XComment("{CMS.AD.STATE}")
    private String state;
    
    @XComment("{CMS.AD.ONLINE_DATE}")
    private LocalDateTime onlineDate;

    @XComment("{CMS.AD.OFFLINE_DATE}")
    private LocalDateTime offlineDate;

    @XComment("{CMS.AD.REDIRECT_URL}")
    private String redirectUrl;

    @XComment("{CMS.AD.LINK}")
    private String link;
    
    @XComment("{CMS.AD.RESOURCE_PATH}")
    private String resourcePath;
    
    @XComment("{CMS.AD.RESOURCE_SRC}")
    private String resourceSrc;

    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
    
    @XComment("{CC.ENTITY.CREATE_BY}")
    private String createBy;
    
    @XComment("{CC.ENTITY.CREATE_TIME}")
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
        this.remark = ad.getRemark();
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
