package com.chestnut.stat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.stereotype.Service;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.stat.IStatType;
import com.chestnut.stat.StatMenu;
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
		this.statTypeMap.values().stream().forEach(st -> menus.addAll(st.getStatMenus()));
		menus.sort((m1, m2) -> m1.sort() - m2.sort());
		List<TreeNode<String>> list = menus.stream().map(m -> {
			return new TreeNode<>(m.menuId(), m.parentId(), I18nUtils.get(m.name()), StringUtils.isEmpty(m.parentId()));
		}).toList();
		return TreeNode.build(list);
	}
}
