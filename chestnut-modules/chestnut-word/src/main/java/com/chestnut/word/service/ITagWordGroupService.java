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
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.domain.dto.CreateTagWordGroupRequest;
import com.chestnut.word.domain.dto.UpdateTagWordGroupRequest;

import java.util.List;
import java.util.function.Consumer;

public interface ITagWordGroupService extends IService<TagWordGroup> {

    /**
	 * 根据分组编码获取分组信息，优先缓存获取
	 *
	 * @param code
	 * @return
	 */
    TagWordGroup getTagWordGroup(String owner, String code);

    /**
	 * 添加TAG词分组
	 * 
	 * @param req
	 * @return
	 */
	TagWordGroup addTagWordGroup(CreateTagWordGroupRequest req);

	/**
	 * 编辑TAG词分组
	 * 
	 * @param req
	 */
	void editTagWordGroup(UpdateTagWordGroupRequest req);

	/**
	 * 校验TAG词分组编码唯一性
	 *
	 * @param owner
	 * @param groupId
	 * @param code
	 */
    void checkUnique(String owner, Long groupId, String code);

    /**
	 * 删除TAG词分组
	 * 
	 * @param groupIds
	 */
	void deleteTagWordGroups(List<Long> groupIds);

	/**
	 * 生成分组树数据
	 * 
	 * @param consumer
	 * @return
	 */
	List<TreeNode<String>> buildTreeData(Consumer<LambdaQueryWrapper<TagWordGroup>> consumer);
}
