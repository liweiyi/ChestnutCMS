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
