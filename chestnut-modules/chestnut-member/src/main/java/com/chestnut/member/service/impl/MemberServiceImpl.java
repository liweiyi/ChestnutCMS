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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.chestnut.common.utils.*;
import com.chestnut.member.config.MemberConfig;
import com.chestnut.member.core.IMemberStatData;
import com.chestnut.member.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.member.domain.dto.MemberDTO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.mapper.MemberLevelExpLogMapper;
import com.chestnut.member.mapper.MemberLevelMapper;
import com.chestnut.member.mapper.MemberMapper;
import com.chestnut.member.mapper.MemberSignInLogMapper;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.service.ISecurityConfigService;

import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService, CommandLineRunner {

	private final MemberLevelMapper memberLevelMapper;

	private final MemberLevelExpLogMapper memberLevelExpLogMapper;

	private final MemberSignInLogMapper memberSignInLogMapper;

	private final ISecurityConfigService securityConfigService;

	@Override
	public void addMember(MemberDTO dto) {
		boolean allBlank = StringUtils.isAllBlank(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail());
		Assert.isFalse(allBlank, MemberErrorCode.USERNAME_PHONE_EMAIL_ALL_EMPTY::exception);

		this.checkMemberUnique(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail(), null);

		Member member = new Member();
		member.setMemberId(IdUtils.getSnowflakeId());
		member.setUserName(dto.getUserName());
		if (StringUtils.isEmpty(member.getUserName())) {
			member.setUserName(CDKeyUtil.genChar56(member.getMemberId(), 10));
		}
		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setNickName(dto.getNickName());
		member.setBirthday(dto.getBirthday());
		member.setPassword(SecurityUtils.passwordEncode(dto.getPassword()));
		member.setStatus(dto.getStatus());
		member.setRemark(dto.getRemark());
		member.createBy(dto.getOperator().getUsername());
		// 校验密码
		this.securityConfigService.validPassword(member, dto.getPassword());
		// 强制首次登陆修改密码
		this.securityConfigService.forceModifyPwdAfterUserAdd(member);
		this.save(member);
	}

	@Override
	public void updateMember(MemberDTO dto) {
		Member member = this.getById(dto.getMemberId());
		Assert.notNull(member, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("memberId", dto.getMemberId()));

		this.checkMemberUnique(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail(), dto.getMemberId());

		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setNickName(dto.getNickName());
		member.setBirthday(dto.getBirthday());
		member.setStatus(dto.getStatus());
		member.setRemark(dto.getRemark());
		member.updateBy(dto.getOperator().getUsername());
		this.updateById(member);
	}

	@Override
	public void deleteMembers(List<Long> memberIds) {
		this.removeByIds(memberIds);
		// 删除会员等级信息
		this.memberLevelMapper.delete(new LambdaQueryWrapper<MemberLevel>().in(MemberLevel::getMemberId, memberIds));
		// 删除会员等级经验日志
		this.memberLevelExpLogMapper
				.delete(new LambdaQueryWrapper<MemberLevelExpLog>().in(MemberLevelExpLog::getMemberId, memberIds));
		// 删除会员签到日志
		this.memberSignInLogMapper
				.delete(new LambdaQueryWrapper<MemberSignInLog>().in(MemberSignInLog::getMemberId, memberIds));
	}

	/**
	 * 重置用户密码
	 */
	@Override
	public void resetPwd(MemberDTO dto) {
		Member member = this.getById(dto.getMemberId());
		Assert.notNull(member, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("memberId", dto.getMemberId()));

		this.securityConfigService.validPassword(member, dto.getPassword());

		member.setPassword(SecurityUtils.passwordEncode(dto.getPassword()));
		member.setUpdateTime(LocalDateTime.now());
		member.setUpdateBy(dto.getOperator().getUsername());
		this.updateById(member);
	}

	/**
	 * 校验用户名、手机号、邮箱是否已存在
	 */
	private void checkMemberUnique(String userName, String phonenumber, String email, Long memberId) {
		Optional<Member> oneOpt = this.lambdaQuery()
				.and(wrapper -> wrapper.eq(StringUtils.isNotEmpty(userName), Member::getUserName, userName).or()
						.eq(StringUtils.isNotEmpty(phonenumber), Member::getPhoneNumber, phonenumber).or()
						.eq(StringUtils.isNotEmpty(email), Member::getEmail, email))
				.ne(IdUtils.validate(memberId), Member::getMemberId, memberId).oneOpt();
		if (oneOpt.isPresent()) {
			Member member = oneOpt.get();
			Assert.isFalse(StringUtils.isNotEmpty(userName) && userName.equals(member.getUserName()),
					MemberErrorCode.USERNAME_CONFLICT::exception);
			Assert.isFalse(StringUtils.isNotEmpty(phonenumber) && phonenumber.equals(member.getPhoneNumber()),
					MemberErrorCode.PHONE_CONFLICT::exception);
			Assert.isFalse(StringUtils.isNotEmpty(email) && email.equals(member.getEmail()),
					MemberErrorCode.EMAIL_CONFLICT::exception);
		}
	}

	@Override
	public String uploadAvatarByBase64(Long memberId, String base64Data) throws IOException {
		if (!base64Data.startsWith("data:image/")) {
			return null;
		}
		String base64Str = StringUtils.substringAfter(base64Data, ",");
		byte[] imageBytes = Base64.getDecoder().decode(base64Str);

		String suffix = base64Data.substring(11, base64Data.indexOf(";"));
		String path = "avatar/" + memberId + "." + suffix;
		FileUtils.writeByteArrayToFile(new File(MemberConfig.getUploadDir() + path), imageBytes);

		log.info("upload avatar: " + path);
		this.lambdaUpdate().set(Member::getAvatar, path).eq(Member::getMemberId, memberId).update();
		return path;
	}

	private final List<IMemberStatData> memberDataStats;

	@Override
	public void run(String... args) {
		Field[] declaredFields = MemberStatData.class.getDeclaredFields();
		List<String> fieldNames = Stream.of(declaredFields).map(Field::getName).toList();
		for (IMemberStatData mds : memberDataStats) {
			if (!fieldNames.contains(mds.getField())) {
				throw new RuntimeException("Member data stat field `"
						+ mds.getClass().getSimpleName() + "." + mds.getField() + "` not exists.");
			}
		}
	}
}