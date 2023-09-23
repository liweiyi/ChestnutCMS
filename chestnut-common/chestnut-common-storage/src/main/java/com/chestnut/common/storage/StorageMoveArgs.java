package com.chestnut.common.storage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StorageMoveArgs {
	
	/**
	 * 访问API接口地址
	 */
	private String endpoint;

	/**
	 * 用户名
	 */
	private String accessKey;

	/**
	 * 访问密码
	 */
	private String accessSecret;
	
	/**
	 * 存储空间名
	 */
	private String bucket;

	/**
	 * 源路径
	 */
	private String sourcePath;
	
	/**
	 * 目标路径
	 */
	private String destPath;
}
