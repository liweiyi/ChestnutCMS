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
package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberExpConfig;
import com.chestnut.member.level.IExpOperation;

import java.util.List;
import java.util.Map;

public interface IMemberExpConfigService extends IService<MemberExpConfig> {
	
	/**
	 * 获取经验值操作项实例
	 * 
	 * @param opType 操作类型
	 * @return IExpOperation
	 */
	IExpOperation getExpOperation(String opType);
	
	/**
	 * 获取经验值操作项实例集合
	 */
	Map<String, IExpOperation> getExpOperations();

	/**
	 * 获取指定等级类型的操作项配置数据
	 * 
	 * @param opType 操作类型
	 * @param levelType 等级类型
	 * @return
	 */
	MemberExpConfig getMemberExpOperation(String opType, String levelType);

	/**
	 * 添加操作项配置
	 * 
	 * @param expOp 操作项配置
	 */
	void addExpOperation(MemberExpConfig expOp);

	/**
	 * 更细操作项配置
	 * 
	 * @param expOp 操作项配置
	 */
	void updateExpOperation(MemberExpConfig expOp);

	/**
	 * 删除操作项配置
	 * 
	 * @param expOperationIds 操作项配置ID
	 */
	void deleteExpOperations(List<Long> expOperationIds);

	/**
	 * 触发操作项，处理操作项配置等级经验值
	 * 
	 * @param expOpType 操作项类型
	 * @param memberId 会员ID
	 */
	void triggerExpOperation(String expOpType, Long memberId);
}