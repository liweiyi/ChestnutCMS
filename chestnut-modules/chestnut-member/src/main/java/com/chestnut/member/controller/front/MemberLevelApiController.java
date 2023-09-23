package com.chestnut.member.controller.front;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Priv(type = MemberUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/level")
public class MemberLevelApiController extends BaseRestController {

	private final IMemberLevelService memberLevelService;

	/**
	 * 获取会员等级数据
	 */
	@GetMapping
	public R<?> getMemberLevels() {
		List<MemberLevel> list = this.memberLevelService.getMemberLevels(StpMemberUtil.getLoginIdAsLong());
		return this.bindDataTable(list);
	}
}