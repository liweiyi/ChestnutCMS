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
package com.chestnut.vote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.vote.domain.VoteSubject;
import com.chestnut.vote.domain.dto.CreateVoteSubjectRequest;
import com.chestnut.vote.domain.dto.SaveSubjectItemsRequest;
import com.chestnut.vote.domain.dto.UpdateVoteSubjectRequest;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public interface IVoteSubjectService extends IService<VoteSubject> {

	/**
	 * 获取指定问卷调查的所有主题列表
	 * 
	 * @param voteId
	 * @return
	 */
	List<VoteSubject> getVoteSubjectList(Long voteId);

	/**
	 * 添加问卷调查主题数据
	 * 
	 * @param req 主题信息
	 */
	void addVoteSubject(CreateVoteSubjectRequest req);

	/**
	 * 更新问卷调查主题数据
	 * 
	 * @param req 主题信息
	 */
	void updateVoteSubject(UpdateVoteSubjectRequest req);

	/**
	 * 删除问卷调查主题数据
	 * 
	 * @param subjectIds 主题ID列表
	 */
	void deleteVoteSubjects(@NotEmpty List<Long> subjectIds);

	/**
	 * 保存主题选项列表
	 * 
	 * @param req 主题选项信息
	 */
	void saveSubjectItems(SaveSubjectItemsRequest req);

}