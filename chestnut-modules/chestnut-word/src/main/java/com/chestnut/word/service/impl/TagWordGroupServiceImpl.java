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
package com.chestnut.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.mapper.TagWordGroupMapper;
import com.chestnut.word.mapper.TagWordMapper;
import com.chestnut.word.service.ITagWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagWordGroupServiceImpl extends ServiceImpl<TagWordGroupMapper, TagWordGroup>
		implements ITagWordGroupService {

	private final TagWordMapper tagWordMapper;

	@Override
	public TagWordGroup addTagWordGroup(TagWordGroup group) {
		checkUnique(group.getOwner(), null, group.getCode());

		group.setGroupId(IdUtils.getSnowflakeId());
		group.setWordTotal(0L);
		group.setSortFlag(SortUtils.getDefaultSortValue());
		this.save(group);
		return group;
	}

	@Override
	public void editTagWordGroup(TagWordGroup group) {
		TagWordGroup dbGroup = this.getById(group.getGroupId());
		Assert.notNull(dbGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));

		checkUnique(dbGroup.getOwner(), dbGroup.getGroupId(), group.getCode());

		dbGroup.setName(group.getName());
		dbGroup.setRemark(group.getRemark());
		dbGroup.updateBy(group.getUpdateBy());
		this.updateById(group);
	}

	@Override
	public void checkUnique(String owner, Long groupId, String code) {
		Long count = this.lambdaQuery().eq(StringUtils.isNotEmpty(owner), TagWordGroup::getOwner, owner)
				.ne(IdUtils.validate(groupId), TagWordGroup::getGroupId, groupId)
				.eq(TagWordGroup::getCode, code)
				.count();
		Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("code"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTagWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.tagWordMapper.delete(new LambdaQueryWrapper<TagWord>().eq(TagWord::getGroupId, groupId));
		}
	}

	@Override
	public List<TreeNode<String>> buildTreeData(List<TagWordGroup> groups) {
		List<TreeNode<String>> list = new ArrayList<>();
		if (groups != null && !groups.isEmpty()) {
			groups.forEach(c -> {
				TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getGroupId()),
						String.valueOf(c.getParentId()), c.getName(), c.getParentId() == 0);
				Map<String, Object> props = Map.of("code", c.getCode());
				treeNode.setProps(props);
				list.add(treeNode);
			});
		}
		return TreeNode.build(list);
	}
}
