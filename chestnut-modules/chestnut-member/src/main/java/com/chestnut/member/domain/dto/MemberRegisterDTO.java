package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class MemberRegisterDTO {
	
	/**
	 * 注册方式：邮箱注册，手机号注册，手机短信验证码注册
	 */
	@NotEmpty
	private String type;
	
	/**
	 * 用户名
	 */
	@Size(min = 6, max = 30, message = "用户名长度必须大于6且小于30个字符")
	@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "用户名必须以字母开头，且只能为（大小写字母，数字，下滑线）")
	private String userName;
	
	/**
	 * 邮箱
	 */
	@Email
	private String email;
	
	/**
	 * 手机号
	 */
	@Pattern(regexp = "^1[3|4|5|6|7|8|9][0-9]\\d{8}$", message = "手机号码格式错误")
	private String phonenumber;

	/**
	 * 用户密码
	 */
	@NotEmpty
	private String password;

	/**
	 * 验证码
	 */
	private String authCode;

	private String ip;

	private String userAgent;
}
