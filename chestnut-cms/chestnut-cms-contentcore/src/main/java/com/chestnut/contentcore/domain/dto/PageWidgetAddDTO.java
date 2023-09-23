package com.chestnut.contentcore.domain.dto;

import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageWidgetAddDTO {

	/**
	 * 栏目ID
	 */
	@NotNull
	@Min(0)
	private Long catalogId;

	/**
	 * 页面部件类型
	 */
	@NotEmpty
	private String type;

	/**
	 * 名称
	 */
	@NotEmpty
	private String name;

	/**
	 * 编码
	 */
	@NotEmpty
	private String code;

	/**
	 * 发布通道编码
	 */
	@NotEmpty
    private String publishPipeCode;

	/**
	 * 模板
	 */
	private String template;

	/**
	 * 静态化目录
	 */
	private String path;
    
	/**
	 * 备注
	 */
    private String remark;
}
