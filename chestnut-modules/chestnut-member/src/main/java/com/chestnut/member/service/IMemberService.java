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
package com.chestnut.member.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.dto.MemberDTO;

public interface IMemberService extends IService<Member> {

	/**
	 * 添加会员信息
	 * 
	 * @param dto
	 * @return
	 */
	void addMember(MemberDTO dto);

	/**
	 * 修改会员信息
	 * 
	 * @param dto
	 * @return
	 */
	void updateMember(MemberDTO dto);
	
	/**
	 * 删除会员信息
	 * 
	 * @param memberIds
	 */
	void deleteMembers(List<Long> memberIds);

	/**
	 * 重置用户密码
	 * 
	 * @param dto 用户信息
	 * @return 结果
	 */
	void resetPwd(MemberDTO dto);

	/**
	 * 上传用户头像
	 *
	 * @param memberId
	 * @param image
	 */
    String uploadAvatarByBase64(Long memberId, String image) throws IOException;
}