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
package com.chestnut.common.storage;

import java.io.InputStream;

public interface IFileStorageType {
	
	public static final String BEAN_NAME_PREIFX = "FileStorageType_";

	/**
	 * 文件存储方式唯一标识
	 */
	public String getType();
	
	/**
	 * 存储方式名称
	 */
	public String getName();
	
	/**
	 * 测试连接
	 */
	default public boolean testConnection(String endpoint, String accessKey, String accessSecret) {
		return true;
	}

	/**
	 * 重置连接客户端
	 * 
	 * @param clientKey
	 * @param args
	 */
	default void reloadClient(String endpoint, String accessKey, String accessSecret) {
		
	}

	/**
	 * 创建存储桶
	 * 
	 * @param minioClientKey
	 * @param bucketName
	 */
	default void createBucket(StorageCreateBucketArgs args) {
		
	}

	default boolean exists(StorageExistArgs args) {
		return true;
	}

	/**
	 * 读取文件
	 */
	public InputStream read(StorageReadArgs args);
	
	/**
	 * 写入文件
	 */
	public void write(StorageWriteArgs args);
	
	/**
	 * 删除文件
	 */
	public void remove(StorageRemoveArgs args);

	/**
	 * 复制文件
	 */
	void copy(StorageCopyArgs args);

	/**
	 * 删除文件
	 */
	void move(StorageMoveArgs args);
}
