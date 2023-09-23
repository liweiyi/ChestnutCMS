package com.chestnut.word.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.mapper.TagWordGroupMapper;
import com.chestnut.word.mapper.TagWordMapper;
import com.chestnut.word.service.ITagWordGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagWordGroupServiceImpl extends ServiceImpl<TagWordGroupMapper, TagWordGroup>
		implements ITagWordGroupService {

	private final TagWordMapper tagWordMapper;

	@Override
	public TagWordGroup addTagWordGroup(TagWordGroup group) {
		checkUnique(group.getParentId(), null, group.getName(), group.getCode());

		group.setGroupId(IdUtils.getSnowflakeId());
		group.setSortFlag(SortUtils.getDefaultSortValue());
		this.save(group);
		return group;
	}

	@Override
	public void editTagWordGroup(TagWordGroup group) {
		TagWordGroup dbGroup = this.getById(group.getGroupId());
		Assert.notNull(dbGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));

		checkUnique(group.getParentId(), dbGroup.getGroupId(), group.getName(), group.getCode());

		dbGroup.setName(group.getName());
		dbGroup.setRemark(group.getRemark());
		dbGroup.updateBy(group.getUpdateBy());
		this.updateById(group);
	}

	private void checkUnique(Long parentId, Long groupId, String name, String code) {
		Long count = this.lambdaQuery().eq(TagWordGroup::getParentId, parentId)
				.ne(groupId != null && groupId > 0, TagWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(TagWordGroup::getName, name).or().eq(TagWordGroup::getCode, code))
				.count();
		Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("name/code"));
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
		if (groups != null && groups.size() > 0) {
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
