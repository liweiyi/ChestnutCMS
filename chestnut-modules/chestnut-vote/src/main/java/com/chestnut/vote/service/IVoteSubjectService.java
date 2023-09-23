package com.chestnut.vote.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.vote.domain.VoteSubject;
import com.chestnut.vote.domain.dto.SaveSubjectItemsDTO;

import jakarta.validation.constraints.NotEmpty;

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
	 * @param voteSubject
	 */
	void addVoteSubject(VoteSubject voteSubject);

	/**
	 * 更新问卷调查主题数据
	 * 
	 * @param voteSubject
	 */
	void updateVoteSubject(VoteSubject voteSubject);

	/**
	 * 删除问卷调查主题数据
	 * 
	 * @param subjectIds
	 */
	void deleteVoteSubjects(@NotEmpty List<Long> subjectIds);

	/**
	 * 保存主题选项列表
	 * 
	 * @param dto
	 */
	void saveSubjectItems(SaveSubjectItemsDTO dto);

}