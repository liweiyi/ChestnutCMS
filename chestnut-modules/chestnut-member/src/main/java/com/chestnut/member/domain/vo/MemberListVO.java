package com.chestnut.member.domain.vo;

import com.chestnut.common.security.domain.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/***
 * 会员表
 */
@Getter
@Setter
public class MemberListVO extends BaseDTO {
	
	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 会员用户名
	 */
	private String userName;
	
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
	 * 来源类型
	 */
	private String sourceType;
	
	/**
	 * 来源ID
	 */
	private String sourceId;
	
	/**
	 * 最近登录IP
	 */
	private String lastLoginIp;
	
	/**
	 * 最近登录时间
	 */
	private LocalDateTime lastLoginTime;

	/**
	 * 注册时间
	 */
	private LocalDateTime createTime;
}
