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
package com.chestnut.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.dto.CreateHotWordGroupRequest;
import com.chestnut.word.domain.dto.UpdateHotWordGroupRequest;
import com.chestnut.word.exception.WordErrorCode;
import com.chestnut.word.mapper.HotWordGroupMapper;
import com.chestnut.word.mapper.HotWordMapper;
import com.chestnut.word.service.IHotWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class HotWordGroupServiceImpl extends ServiceImpl<HotWordGroupMapper, HotWordGroup>
		implements IHotWordGroupService {

	private final HotWordMapper hotWordMapper;

	@Override
	public HotWordGroup addHotWordGroup(CreateHotWordGroupRequest req) {
		this.checkUnique(req.getOwner(), null, req.getCode());

		HotWordGroup word = new HotWordGroup();
		word.setGroupId(IdUtils.getSnowflakeId());
		word.setOwner(req.getOwner());
		word.setName(req.getName());
		word.setCode(req.getCode());
		word.setWordTotal(0L);
		word.setSortFlag(SortUtils.getDefaultSortValue());
		word.setRemark(req.getRemark());
		word.createBy(req.getOperator().getUsername());
		this.save(word);
		return word;
	}

	@Override
	public void updateHotWordGroup(UpdateHotWordGroupRequest req) {
		HotWordGroup db = this.getById(req.getGroupId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", req.getGroupId()));

		this.checkUnique(db.getOwner(), db.getGroupId(), req.getCode());

		db.setName(req.getName());
		db.setCode(req.getCode());
        db.setSortFlag(req.getSortFlag());
		db.setRemark(req.getRemark());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
	}

	@Override
	public void deleteHotWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.hotWordMapper.delete(new LambdaQueryWrapper<HotWord>().eq(HotWord::getGroupId, groupId));
		}
	}

	@Override
	public void checkUnique(String owner, Long groupId, String code) {
		Long count = this.lambdaQuery()
				.eq(StringUtils.isNotEmpty(owner), HotWordGroup::getOwner, owner)
				.ne(IdUtils.validate(groupId), HotWordGroup::getGroupId, groupId)
				.eq(HotWordGroup::getCode, code).count();
		Assert.isTrue(count == 0, WordErrorCode.CONFLIECT_HOT_WORD_GROUP::exception);
	}

    @Override
    public List<TreeNode<String>> getGroupTreeData(Consumer<LambdaQueryWrapper<HotWordGroup>> consumer) {
        LambdaQueryWrapper<HotWordGroup> q = new LambdaQueryWrapper<HotWordGroup>().orderByAsc(HotWordGroup::getSortFlag);
        consumer.accept(q);
        List<HotWordGroup> groups = this.list(q);
        List<TreeNode<String>> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(groups)) {
            groups.forEach(c -> {
                TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getGroupId()), "", c.getName(), true);
                treeNode.setProps(Map.of(
                        "code", c.getCode(),
                        "sort", c.getSortFlag(),
                        "remark", Objects.requireNonNullElse(c.getRemark(), "")
                ));
                list.add(treeNode);
            });
        }
        return TreeNode.build(list);
    }
}
