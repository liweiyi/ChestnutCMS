package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogAddDTO extends BaseDTO {
	
	/**
	 * 所属站点ID
	 */
	private Long siteId;

    /**
     * 父级栏目ID
     */
    private Long parentId = 0L;

    /**
     * 栏目名称 
     */
    @NotBlank
    private String name;
    
    /**
     * 栏目别名
     */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "栏目别名只能使用大小写字母、数字、下划线组合")
    private String alias;
    
    /**
     * 栏目目录
     */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_\\/]+$", message = "栏目路径只能使用大小写字母、数字、下划线组合")
    private String path;

    /**
     * 栏目类型
     */
    @NotBlank
    private String catalogType;
}
