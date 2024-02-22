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
package com.chestnut.stat.service;

import java.util.List;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.stat.core.IStatType;

public interface IStatService {

	/**
	 * 获取指定统计菜单类型
	 * 
	 * @param typeId
	 * @return
	 */
	IStatType getStatType(String typeId);

	/**
	 * 获取统计菜单类型列表
	 */
	List<IStatType> getStatTypes();

	/**
	 * 获取统计菜单树
	 * 
	 * @return
	 */
	List<TreeNode<String>> getStatMenuTree();
}
