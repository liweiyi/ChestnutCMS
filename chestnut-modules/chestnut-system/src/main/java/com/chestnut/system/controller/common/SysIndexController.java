package com.chestnut.system.controller.common;

import com.chestnut.common.config.properties.ChestnutProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * 首页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
public class SysIndexController {
	
	private final ChestnutProperties properties;

	/**
	 * 访问首页，提示语
	 */
	@RequestMapping("/")
	public String index() {
		return StringUtils.messageFormat("欢迎使用{0}，当前版本：v{1}，请通过前端地址访问。", properties.getName(), properties.getVersion());
	}
}
