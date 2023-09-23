package com.chestnut.search.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SearchLogDTO {

	/**
     * 搜索词
     */
	@NotBlank
    private String word;
    
    /**
     * Header:UserAgent
     */
    private String userAgent;
    
    /**
     * IP地址
     */
    private String ip;

    private String location;
    
    /**
     * Header:Referer
     */
    private String referer;

    private LocalDateTime logTime;

    private String source;
}