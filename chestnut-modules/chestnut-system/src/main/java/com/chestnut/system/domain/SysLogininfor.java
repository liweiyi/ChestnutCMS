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
package com.chestnut.system.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.chestnut.common.utils.poi.converter.LocalDateTimeConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * 系统访问记录表 sys_logininfor
 */
@Getter
@Setter
@TableName(SysLogininfor.TABLE_NAME)
public class SysLogininfor implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_logininfor";

	/** ID */
	@ExcelProperty("日志ID")
	@TableId(value = "info_id", type = IdType.AUTO)
	private Long infoId;

	@ExcelProperty("用户类型")
	private String userType;

	@ExcelProperty("用户ID")
	private String userId;

	/** 用户账号 */
	@ExcelProperty("用户账号")
	private String userName;
	
	@ExcelProperty("日志类型")
	private String logType;

	/** 结果状态 0成功 1失败 */
	@ExcelProperty("结果状态")
	private String status;

	/** 登录IP地址 */
	@ExcelProperty("登录地址")
	private String ipaddr;

	/** 登录地点 */
	@ExcelProperty("登录地点")
	private String loginLocation;

	/** 浏览器类型 */
	@ExcelProperty("浏览器")
	private String browser;

	/** 操作系统 */
	@ExcelProperty("操作系统")
	private String os;

	/** 提示消息 */
	@ExcelProperty("提示消息")
	private String msg;

	/** 访问时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ExcelProperty(value = "访问时间", converter = LocalDateTimeConverter.class)
	private LocalDateTime loginTime;

	/** 请求参数 */
	@ExcelIgnore
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@TableField(exist = false)
	private Map<String, Object> params;

	public Map<String, Object> getParams() {
		if (params == null) {
			params = new HashMap<>();
		}
		return params;
	}
}
