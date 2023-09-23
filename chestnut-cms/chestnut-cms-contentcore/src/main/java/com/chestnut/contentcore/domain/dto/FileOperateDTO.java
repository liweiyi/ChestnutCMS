package com.chestnut.contentcore.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileOperateDTO {
	
	/*
	 * 文件路径
	 */
	@NotNull
	private String filePath;

	/*
	 * 重名名名称
	 */
	private String rename;
	
	/*
	 * 文件内容
	 */
	private String fileContent;
	
}
