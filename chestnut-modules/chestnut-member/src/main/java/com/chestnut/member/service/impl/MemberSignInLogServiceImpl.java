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
package com.chestnut.member.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.chestnut.member.level.impl.SignInExpOperation;
import com.chestnut.member.service.IMemberExpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.member.domain.MemberSignInLog;
import com.chestnut.member.domain.dto.MemberComplementHistoryDTO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.mapper.MemberSignInLogMapper;
import com.chestnut.member.service.IMemberSignInLogService;

@Service
@RequiredArgsConstructor
public class MemberSignInLogServiceImpl extends ServiceImpl<MemberSignInLogMapper, MemberSignInLog>
		implements IMemberSignInLogService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final IMemberExpConfigService memberExpConfigService;

	@Override
	public void doSignIn(LoginUser loginUser) {
		LocalDateTime now = LocalDateTime.now();
		Integer signInKey = Integer.valueOf(now.format(FORMATTER));

		Long count = this.lambdaQuery().eq(MemberSignInLog::getMemberId, loginUser.getUserId())
				.eq(MemberSignInLog::getSignInKey, signInKey).count();
		Assert.isTrue(count == 0, MemberErrorCode.SIGN_IN_COMPLETED::exception);
		
		MemberSignInLog signInLog = new MemberSignInLog();
		signInLog.setMemberId(loginUser.getUserId());
		signInLog.setSignInKey(signInKey);
		signInLog.setLogTime(now);
		this.save(signInLog);
		// 触发会员经验值操作
		this.memberExpConfigService.triggerExpOperation(SignInExpOperation.ID, signInLog.getMemberId());
	}

	@Override
	public void complementHistory(MemberComplementHistoryDTO dto) {
		LocalDateTime signInDay = LocalDateTime.of(dto.getYear(), dto.getMonth(), dto.getDay(), 0, 0, 0);
		Integer signInKey = Integer.valueOf(signInDay.format(FORMATTER));

		Long count = this.lambdaQuery().eq(MemberSignInLog::getMemberId, dto.getOperator().getUserId())
				.eq(MemberSignInLog::getSignInKey, signInKey).count();
		Assert.isTrue(count == 0, MemberErrorCode.SIGN_IN_COMPLETED::exception);
		
		MemberSignInLog signInLog = new MemberSignInLog();
		signInLog.setMemberId(dto.getOperator().getUserId());
		signInLog.setSignInKey(signInKey);
		signInLog.setLogTime(LocalDateTime.now());
		this.save(signInLog);
		// 触发会员经验值操作
		this.memberExpConfigService.triggerExpOperation(SignInExpOperation.ID, signInLog.getMemberId());
	}
}