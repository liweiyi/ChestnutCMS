package com.chestnut.contentcore.domain.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TemplateListVO {

    private Long templateId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 发布通道编码
     */
    private String publishPipeCode;

    /**
     * 模板文件路径
     */
    private String path;

    /**
     * 模板文件大小
     */
    private Long filesize;
    
    private String filesizeName;
    
    /**
     * 模板文件最后变更时间
     */
    private LocalDateTime modifyTime;
}
