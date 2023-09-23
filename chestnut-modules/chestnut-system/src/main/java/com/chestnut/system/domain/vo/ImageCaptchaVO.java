package com.chestnut.system.domain.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ImageCaptchaVO {

	/**
	 * 是否开启验证码
	 */
	private boolean captchaEnabled;
	
	/**
	 * 用来标识一个用户
	 */
	private String uuid;
	
	/**
	 * Base64编码图片
	 */
	private String img;
}
