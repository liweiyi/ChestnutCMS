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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.perms.PrivItem;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CatalogPrivVO {

	private Long catalogId;
	
	private Long parentId;

	private String name;
	
	private List<CatalogPrivVO> children;

	/**
	 * 拥有的权限项
	 */
	private Map<String, PrivItem> perms = new HashMap<>();
}
