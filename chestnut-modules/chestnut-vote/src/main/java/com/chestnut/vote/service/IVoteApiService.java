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
package com.chestnut.vote.service;

import com.chestnut.vote.domain.dto.VoteSubmitDTO;
import com.chestnut.vote.domain.vo.VoteVO;

/**
 * 问卷调查前台业务服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IVoteApiService {

	/**
	 * 获取问卷调查详情
	 * 
	 * @param voteId
	 * @return
	 */
	VoteVO getVote(Long voteId);

	/**
	 * 提交问卷调查
	 * 
	 * @param dto
	 */
	void submitVote(VoteSubmitDTO dto);
}
