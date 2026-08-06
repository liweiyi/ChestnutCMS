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
package com.chestnut.system.domain;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.system.fixed.dict.EnableOrDisable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门表 sys_dept
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{ENT.SYS.DEPT}")
@TableName(SysDept.TABLE_NAME)
public class SysDept extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_dept";

	/** 部门ID */
	@XComment("{ENT.SYS.DEPT.ID}")
	@TableId(value = "dept_id", type = IdType.INPUT)
	private Long deptId;

	/** 父部门ID */
	@XComment("{ENT.SYS.DEPT.PARENT_ID}")
	private Long parentId;

	/** 祖级列表 */
	@XComment("{ENT.SYS.DEPT.ANCESTORS}")
	private String ancestors;

	/** 部门名称 */
	@XComment("{ENT.SYS.DEPT.NAME}")
	private String deptName;

	/** 显示顺序 */
	@XComment("{CC.ENTITY.SORT}")
	private Integer orderNum;

	/** 负责人 */
	@XComment("{ENT.SYS.DEPT.LEADER}")
	private String leader;

	/** 联系电话 */
	@XComment("{ENT.SYS.DEPT.PHONE}")
	private String phone;

	/** 邮箱 */
	@XComment("{ENT.SYS.DEPT.EMAIL}")
	private String email;

	/** 部门状态:EnableOrDisable */
	@XComment("{CC.ENTITY.STATUS}")
	private String status;

	/** 父部门名称 */
	@XComment("{ENT.SYS.DEPT.PARENT_NAME}")
	@TableField(exist = false)
	private String parentName;

	/** 子部门 */
	@XComment("{ENT.SYS.DEPT.CHILDREN}")
	@TableField(exist = false)
	private List<SysDept> children = new ArrayList<SysDept>();

	@NotBlank(message = "{VALID.SYS.DEPT.DEPT_NAME_NOT_BLANK}")
	@Size(min = 0, max = 30, message = "{VALID.SYS.DEPT.DEPT_NAME_SIZE}")
	public String getDeptName() {
		return deptName;
	}

	@NotNull(message = "{VALID.SYS.DEPT.ORDER_NUM_NOT_NULL}")
	public Integer getOrderNum() {
		return orderNum;
	}

	@Size(min = 0, max = 11, message = "{VALID.SYS.DEPT.PHONE_SIZE}")
	public String getPhone() {
		return phone;
	}

	@Email(message = "{VALID.SYS.DEPT.EMAIL_INVALID}")
	@Size(min = 0, max = 50, message = "{VALID.SYS.DEPT.EMAIL_SIZE}")
	public String getEmail() {
		return email;
	}
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
