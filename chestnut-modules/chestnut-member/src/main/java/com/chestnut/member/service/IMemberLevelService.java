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

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberLevel;

public interface IMemberLevelService extends IService<MemberLevel> {

	/**
	 * 获取会员指定类型等级数据
	 * 
	 * @param memberId
	 * @param levelType
	 */
	MemberLevel getMemberLevel(Long memberId, String levelType);
	
	/**
	 * 获取会员所有等级信息
	 * 
	 * @param memberId
	 * @return
	 */
	List<MemberLevel> getMemberLevels(Long memberId);
}