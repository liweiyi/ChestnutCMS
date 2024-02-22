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

import java.util.List;

public class StorageListResult<T> {

	private List<T> objects; 
	
	/**
	 * 列举文件使用的continuationToken
	 */
	private String nextContinuationToken ;
	
	public String getNextContinuationToken() {
		return nextContinuationToken;
	}

	public void setNextContinuationToken(String nextContinuationToken) {
		this.nextContinuationToken = nextContinuationToken;
	}

	public List<T> getObjects() {
		return objects;
	}

	public void setObjects(List<T> objects) {
		this.objects = objects;
	}
}
