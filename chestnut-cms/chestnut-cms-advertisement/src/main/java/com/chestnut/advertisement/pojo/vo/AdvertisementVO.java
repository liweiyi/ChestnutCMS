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

    @XComment("广告ID")
    private Long advertisementId;
	
    @XComment("所属广告版本（页面部件）ID")
	private Long adSpaceId;
	
    @XComment("类型")
    private String type;

    @XComment("名称")
    private String name;

    @XComment("权重")
    private Integer weight;

    @XComment("关键词")
    private String keywords;

    @XComment("状态")
    private String state;
    
    @XComment("上线时间")
    private LocalDateTime onlineDate;

    @XComment("下线时间")
    private LocalDateTime offlineDate;

    @XComment("原始跳转链接")
    private String redirectUrl;

    @XComment("实际跳转链接（可设置为中转地址）")
    private String link;
    
    @XComment("素材路径")
    private String resourcePath;
    
    @XComment("素材访问链接")
    private String resourceSrc;
    
    @XComment("创建人")
    private String createBy;
    
    @XComment("创建时间")
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
