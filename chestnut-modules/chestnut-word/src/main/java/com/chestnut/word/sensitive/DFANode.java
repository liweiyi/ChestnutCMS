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
package com.chestnut.word.sensitive;

import java.util.Map;

public class DFANode<T> {
	
	/**
	 * 上级节点Key
	 */
	String parentKey;
	
	/**
	 * 节点Key
	 */
	String key;
	
	/**
	 * 是否叶子节点
	 */
	boolean end = false;
	
	/**
	 * 叶子节点记录词类型
	 */
	T data;
	
	/**
	 * 子节点
	 */
	Map<String, DFANode<T>> children;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append("{")
				.append("key=").append(key).append(", parent=").append(parentKey).append(", end=").append(end);
		if (data != null) {
			sb.append(", data=").append(data.toString());
		}
		if (this.children != null) {
			sb.append(", children=").append(this.children.toString());
		}
		sb.append("}");
		return sb.toString();
	}
}