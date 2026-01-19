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
package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.dto.CreateMemberRequest;
import com.chestnut.member.domain.dto.ResetMemberPasswordRequest;
import com.chestnut.member.domain.dto.UpdateMemberRequest;

import java.io.IOException;
import java.util.List;

public interface IMemberService extends IService<Member> {

	/**
	 * 添加会员信息
	 * 
	 * @param req 会员信息
	 */
	void addMember(CreateMemberRequest req);

	/**
	 * 修改会员信息
	 * 
	 * @param req 会员信息
	 */
	void updateMember(UpdateMemberRequest req);
	
	/**
	 * 删除会员信息
	 * 
	 * @param memberIds 会员ID列表
	 */
	void deleteMembers(List<Long> memberIds);

	/**
	 * 重置用户密码
	 */
	void resetPwd(ResetMemberPasswordRequest req);

	/**
	 * 上传用户头像
	 *
	 * @param memberId 会员ID
	 * @param image 图片
	 */
    String uploadAvatarByBase64(Long memberId, String image) throws IOException;
}