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
package com.chestnut.word.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.dto.CreateHotWordGroupRequest;
import com.chestnut.word.domain.dto.UpdateHotWordGroupRequest;

import java.util.List;
import java.util.function.Consumer;

public interface IHotWordGroupService extends IService<HotWordGroup> {

	/**
	 * 添加热词分组
	 * 
	 * @param req
	 */
	HotWordGroup addHotWordGroup(CreateHotWordGroupRequest req);

	/**
	 * 修改热词分组
	 * 
	 * @param req
	 */
	void updateHotWordGroup(UpdateHotWordGroupRequest req);

	/**
	 * 删除热词分组
	 * 
	 * @param groupIds
	 */
	void deleteHotWordGroups(List<Long> groupIds);

	/**
	 * 校验热词分组编码唯一性
	 *
	 * @param owner
	 * @param groupId
	 * @param code
	 */
    void checkUnique(String owner, Long groupId, String code);

    /**
     * 获取分组树数据
     *
     * @param consumer
     * @return
     */
    List<TreeNode<String>> getGroupTreeData(Consumer<LambdaQueryWrapper<HotWordGroup>> consumer);
}
