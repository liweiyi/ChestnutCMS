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