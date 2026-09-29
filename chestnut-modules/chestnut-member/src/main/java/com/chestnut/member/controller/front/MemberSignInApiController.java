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
package com.chestnut.member.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.member.domain.MemberSignInLog;
import com.chestnut.member.domain.dto.MemberComplementHistoryRequest;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberSignInLogService;
import com.chestnut.system.annotation.IgnoreDemoMode;


import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@XComment("{API.DOC.MEMBER.SIGN_IN_API_MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/signIn")
public class MemberSignInApiController extends BaseRestController {

	private final IMemberSignInLogService memberSignInLogService;

	@XComment("{API.DOC.MEMBER.GET_SIGN_IN_LOG}")
	@Priv(type = MemberUserType.TYPE)
	@GetMapping
	public R<?> getMonthSignInLog(@RequestParam(value = "year", required = false) @XComment("{API.DOC.MEMBER.SIGN_IN_YEAR}") Integer year,
			@RequestParam(value = "month", required = false) @XComment("{API.DOC.MEMBER.SIGN_IN_MONTH}") Integer month) {
		LocalDate now = LocalDate.now();
		if (Objects.isNull(year)) {
			year = now.getYear();
		}
		if (Objects.isNull(month)) {
			month = now.getMonthValue();
		}
		List<MemberSignInLog> list = memberSignInLogService.list(
				monthSignInLogQuery(StpMemberUtil.getLoginIdAsLong(), YearMonth.of(year, month)));
		return this.bindDataTable(list);
	}

	/**
	 * 按实际签到日期查询整月记录。补签记录的 logTime 是补签操作时间，可能落在另一个月，
	 * 因此只能用 yyyyMMdd 格式的 signInKey 筛选。月初和月末均包含在查询范围内。
	 */
	static LambdaQueryWrapper<MemberSignInLog> monthSignInLogQuery(long memberId, YearMonth month) {
		int firstDayKey = Integer.parseInt(month.atDay(1).format(DateUtils.FORMAT_YYYYMMDD));
		int lastDayKey = Integer.parseInt(month.atEndOfMonth().format(DateUtils.FORMAT_YYYYMMDD));
		return new LambdaQueryWrapper<MemberSignInLog>()
				.eq(MemberSignInLog::getMemberId, memberId)
				.ge(MemberSignInLog::getSignInKey, firstDayKey)
				.le(MemberSignInLog::getSignInKey, lastDayKey);
	}

	@XComment("{API.DOC.MEMBER.SIGN_IN}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping
	public R<?> signIn() {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberSignInLogService.doSignIn(memberId);
		return R.ok();
	}

	@XComment("{API.DOC.MEMBER.RETROACTIVE_SIGN_IN}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/retroactive")
	public R<?> complementHistory(@RequestBody @Validated MemberComplementHistoryRequest req) {
		this.memberSignInLogService.complementHistory(req);
		return R.ok();
	}
}
