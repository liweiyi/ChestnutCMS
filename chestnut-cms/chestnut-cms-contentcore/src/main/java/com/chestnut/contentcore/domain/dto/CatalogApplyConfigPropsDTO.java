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
package com.chestnut.contentcore.domain.dto;

import java.util.List;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.CATALOG.APPLY_CONFIG_PROPS_DTO}")
@Getter
@Setter
public class CatalogApplyConfigPropsDTO extends BaseDTO {

	/*
	 * 源栏目ID
	 */
	@XComment("{API.DOC.CMS.CATALOG.CATALOG_ID}")
	@LongId
	public Long catalogId;

	/*
	 * 指定更新的目标栏目Ids，为空表示更新所有子栏目
	 */
	@XComment("{API.DOC.CMS.CATALOG.TO_CATALOG_IDS}")
	public List<Long> toCatalogIds;

	/*
	 * 是否覆盖所有扩展属性配置
	 */
	@XComment("{API.DOC.CMS.CATALOG.ALL_EXTENDS}")
	public boolean allExtends;
	
	/*
	 * 指定应用的扩展属性Keys
	 */
	@XComment("{API.DOC.CMS.CATALOG.CONFIG_PROP_KEYS}")
	public List<String> configPropKeys;
}
