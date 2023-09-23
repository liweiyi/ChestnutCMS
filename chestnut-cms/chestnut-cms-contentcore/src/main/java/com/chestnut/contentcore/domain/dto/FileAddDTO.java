package com.chestnut.contentcore.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAddDTO {
	
	/*
	 * 目所在录
	 */
	@NotEmpty
	private String dir;

	/*
	 * 文件/目录名
	 */
	@NotEmpty
	private String fileName;

	/*
	 * 是否目录
	 */
	@NotNull
	private Boolean isDirectory;
	
}
