package com.chestnut.common.storage;

import java.io.InputStream;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StorageWriteArgs {
	
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
	 * 路径
	 */
	private String path;
	
	/**
	 * 写入对象输入流
	 */
	private InputStream inputStream;
	
	/**
	 * 文件大小
	 */
	private Long length;
}
