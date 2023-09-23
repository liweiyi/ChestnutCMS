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