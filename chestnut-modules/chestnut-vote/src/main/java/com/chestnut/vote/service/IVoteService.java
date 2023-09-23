package com.chestnut.vote.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.vote.core.IVoteItemType;
import com.chestnut.vote.core.IVoteUserType;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.VoteLog;
import com.chestnut.vote.domain.vo.VoteVO;

public interface IVoteService extends IService<Vote> {

	/**
	 * 添加问卷调查
	 * 
	 * @param vote
	 * @return
	 */
	void addVote(Vote vote);
	
	/**
	 * 修改问卷调查
	 * 
	 * @param vote
	 */
	void updateVote(Vote vote);
	
	/**
	 * 删除问卷调查
	 * 
	 * @param voteIds
	 */
	void deleteVotes(List<Long> voteIds);

	/**
	 * 获取问卷调查详情（缓存）
	 * 
	 * @param voteId
	 * @return
	 */
	VoteVO getVote(Long voteId);

	/**
	 * 删除指定问卷调查缓存
	 * 
	 * @param voteId
	 */
	void clearVoteCache(Long voteId);

	/**
	 * 获取问卷调查参与用户类型
	 * 
	 * @param type
	 * @return
	 */
	IVoteUserType getVoteUserType(String type);

	/**
	 * 获取问卷调查选项类型
	 * 
	 * @param type
	 * @return
	 */
	IVoteItemType getVoteItemType(String type);

	/**
	 * 问卷提交处理逻辑，问卷参数人数更新，主题选项票数更新
	 * 
	 * @param voteLog
	 */
	void onVoteSubmit(VoteLog voteLog);
}