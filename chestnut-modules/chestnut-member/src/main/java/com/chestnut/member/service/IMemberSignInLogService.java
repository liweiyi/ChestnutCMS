package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.member.domain.MemberSignInLog;
import com.chestnut.member.domain.dto.MemberComplementHistoryDTO;

public interface IMemberSignInLogService extends IService<MemberSignInLog> {

	/**
	 * 签到
	 * 
	 * @param loginUser
	 */
	void doSignIn(LoginUser loginUser);
	
	/**
	 * 补签
	 * 
	 * @param dto
	 */
	void complementHistory(MemberComplementHistoryDTO dto);
}