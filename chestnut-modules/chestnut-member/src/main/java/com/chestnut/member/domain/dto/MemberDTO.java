package com.chestnut.member.domain.dto;

import java.time.LocalDateTime;

import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员操作dto
 */
@Getter
@Setter
public class MemberDTO extends BaseDTO {
	
	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 会员用户名
	 */
	private String userName;
	
	/**
	 * 会员密码
	 */
	private String password;
	
	/**
	 * 昵称
	 */
	private String nickName;
	
	/**
	 * 手机号
	 */
	private String phoneNumber;
	
	/**
	 * Email
	 */
	private String email;
	
	/**
	 * 出生日期
	 */
	private LocalDateTime birthday;
	
	/**
	 * 状态
	 */
	private String status; 
	
	/**
	 * 备注
	 */
	private String remark;
}
