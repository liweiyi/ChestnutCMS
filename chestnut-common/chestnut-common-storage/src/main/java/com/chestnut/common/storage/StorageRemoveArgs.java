/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
public class StorageRemoveArgs extends StorageBasicArgs {
	
	/**
	 * 存储空间名
	 */
	private String bucket;

	/**
	 * 路径
	 */
	private String path;

	public static class Builder {

		private String endpoint;
		private String region;
		private String accessKey;
		private String accessSecret;
		private String bucket;
		private String path;

		public StorageRemoveArgs build() {
			StorageRemoveArgs args = new StorageRemoveArgs();
			args.setEndpoint(endpoint);
			args.setRegion(region);
			args.setAccessKey(accessKey);
			args.setAccessSecret(accessSecret);
			args.setBucket(bucket);
			args.setPath(path);
			return args;
		}

		public Builder endpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}
		public Builder region(String region) {
			this.region = region;
			return this;
		}
		public Builder accessKey(String accessKey) {
			this.accessKey = accessKey;
			return this;
		}
		public Builder accessSecret(String accessSecret) {
			this.accessSecret = accessSecret;
			return this;
		}
		public Builder bucket(String bucket) {
			this.bucket = bucket;
			return this;
		}
		public Builder path(String path) {
			this.path = path;
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
