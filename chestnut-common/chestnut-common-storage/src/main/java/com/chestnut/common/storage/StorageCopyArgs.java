/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageCopyArgs extends StorageBasicArgs {
	
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

	public static class Builder {

		private String endpoint;
		private String region;
		private String accessKey;
		private String accessSecret;
		private String bucket;
		private String sourcePath;
		private String destPath;

		public StorageCopyArgs build() {
			StorageCopyArgs storageCopyArgs = new StorageCopyArgs();
			storageCopyArgs.setEndpoint(endpoint);
			storageCopyArgs.setRegion(region);
			storageCopyArgs.setAccessKey(accessKey);
			storageCopyArgs.setAccessSecret(accessSecret);
			storageCopyArgs.setBucket(bucket);
			storageCopyArgs.setSourcePath(sourcePath);
			storageCopyArgs.setDestPath(destPath);
			return storageCopyArgs;
		}

		public StorageCopyArgs.Builder endpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}
		public StorageCopyArgs.Builder region(String region) {
			this.region = region;
			return this;
		}
		public StorageCopyArgs.Builder accessKey(String accessKey) {
			this.accessKey = accessKey;
			return this;
		}
		public StorageCopyArgs.Builder accessSecret(String accessSecret) {
			this.accessSecret = accessSecret;
			return this;
		}
		public StorageCopyArgs.Builder bucket(String bucket) {
			this.bucket = bucket;
			return this;
		}
		public StorageCopyArgs.Builder sourcePath(String sourcePath) {
			this.sourcePath = sourcePath;
			return this;
		}
		public StorageCopyArgs.Builder destPath(String destPath) {
			this.destPath = destPath;
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
