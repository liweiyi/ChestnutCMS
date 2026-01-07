/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.member.controller.front;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.member.domain.MemberSignInLog;
import com.chestnut.member.domain.dto.MemberComplementHistoryRequest;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberSignInLogService;
import com.chestnut.system.annotation.IgnoreDemoMode;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/signIn")
public class MemberSignInApiController extends BaseRestController {

	private final IMemberSignInLogService memberSignInLogService;

	/**
	 * 获取会员指定月份签到数据，默认：当前月份
	 */
	@Priv(type = MemberUserType.TYPE)
	@GetMapping
	public R<?> getMonthSignInLog(@RequestParam(value = "year", required = false) Integer year,
			@RequestParam(value = "month", required = false) Integer month) {
		LocalDateTime now = LocalDateTime.now();
		if (Objects.isNull(year)) {
			year = now.getYear();
		}
		if (Objects.isNull(month)) {
			month = now.getMonthValue();
		}
		LocalDateTime startTime = LocalDateTime.of(year, month, 1, 0, 0, 0);

		int endYear = year;
		int endMonth = month;
		if (month == 12) {
			endYear = year + 1;
			endMonth = 1;
		}
		LocalDateTime endTime = LocalDateTime.of(endYear, endMonth, 1, 0, 0, 0);
		List<MemberSignInLog> list = this.memberSignInLogService.lambdaQuery()
				.eq(MemberSignInLog::getMemberId, StpMemberUtil.getLoginId()).ge(MemberSignInLog::getLogTime, startTime)
				.lt(MemberSignInLog::getLogTime, endTime).list();
		return this.bindDataTable(list);
	}

	/**
	 * 签到
	 */
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping
	public R<?> signIn() {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberSignInLogService.doSignIn(memberId);
		return R.ok();
	}

	/**
	 * 补签
	 */
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/retroactive")
    @PutMapping
	public R<?> complementHistory(@RequestBody @Validated MemberComplementHistoryRequest req) {
		this.memberSignInLogService.complementHistory(req);
		return R.ok();
	}
}