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
package com.chestnut.stat.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.stereotype.Service;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.stat.core.IStatType;
import com.chestnut.stat.core.StatMenu;
import com.chestnut.stat.errorcode.StatErrorCode;
import com.chestnut.stat.service.IStatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements IStatService {

	private final Map<String, IStatType> statTypeMap;
	
	@Override
	public IStatType getStatType(String typeId) {
		IStatType statType = this.statTypeMap.get(typeId);
		Assert.notNull(statType, () -> StatErrorCode.UNSUPPORT_STAT_TYPE.exception(typeId));
		return statType;
	}

	@Override
	public List<IStatType> getStatTypes() {
		return this.statTypeMap.values().stream().toList();
	}

	@Override
	public List<TreeNode<String>> getStatMenuTree() {
		List<StatMenu> menus = new ArrayList<>();
		this.statTypeMap.values().forEach(st -> menus.addAll(st.getStatMenus()));
		menus.sort(Comparator.comparingInt(StatMenu::sort));
		List<TreeNode<String>> list = menus.stream().map(m -> {
			return new TreeNode<>(m.menuId(), m.parentId(), I18nUtils.get(m.name()), StringUtils.isEmpty(m.parentId()));
		}).toList();
		return TreeNode.build(list);
	}
}
