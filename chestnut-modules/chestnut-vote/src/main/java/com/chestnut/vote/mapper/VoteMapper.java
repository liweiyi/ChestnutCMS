package com.chestnut.vote.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.vote.domain.Vote;

/**
 * <p>
 * 问卷调查表Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface VoteMapper extends BaseMapper<Vote> {

	/**
	 * 参与数+1
	 * 
	 * @param voteId
	 * @return
	 */
	@Update("UPDATE " + Vote.TABLE_NAME + " SET total = total + 1 WHERE vote_id = #{voteId}")
	public int incrVoteTotal(@Param("voteId") Long voteId);
}