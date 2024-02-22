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
 * 操作日志记录表 oper_log
 */
@Getter
@Setter
@TableName(SysOperLog.TABLE_NAME)
public class SysOperLog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_oper_log";

	/** 日志主键 */
	@ExcelProperty("日志ID")
	@TableId(value = "oper_id", type = IdType.AUTO)
	private Long operId;

	@ExcelProperty("操作模块")
	private String title;

	@ExcelProperty("业务类型")
	private String businessType;

	@ExcelProperty("请求方式")
	private String method;

	@ExcelProperty("请求访问方法")
	private String requestMethod;

	@ExcelProperty("操作人类型")
	private String operatorType;

	/** 操作人员ID */
	@ExcelProperty("操作人UID")
	private Long operUid;

	/** 操作人员用户名 */
	@ExcelProperty("操作人用户名")
	private String operName;

	/** 部门名称 */
	@ExcelProperty("部门名称")
	private String deptName;

	/** 请求url */
	@ExcelProperty("请求地址")
	private String operUrl;

	/** 操作地址 */
	@ExcelProperty("IP")
	private String operIp;

	/** 操作地点 */
	@ExcelProperty("操作地点")
	private String operLocation;

	@ExcelProperty("Header[User-Agent]")
	private String userAgent;

	/** 请求参数 */
	@ExcelProperty("请求参数")
	private String requestArgs;

	/** 返回参数 */
	@ExcelProperty("响应结果")
	private String responseResult;

	/** 操作状态（0正常 1异常） */
	@ExcelProperty("响应状态")
	private Integer responseCode;

	/** 操作时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ExcelProperty(value = "操作时间", converter = LocalDateTimeConverter.class)
	private LocalDateTime operTime;

	@ExcelProperty("操作耗时（毫秒）")
	private Long cost;

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
