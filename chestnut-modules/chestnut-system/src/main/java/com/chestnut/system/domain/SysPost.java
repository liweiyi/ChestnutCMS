/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 岗位表 sys_post
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysPost.TABLE_NAME)
public class SysPost extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_post";

	@ExcelProperty("{ENT.SYS.POST.ID}")
	@TableId(value = "post_id", type = IdType.INPUT)
	private Long postId;

	/** 岗位编码 */
	@ExcelProperty("{ENT.SYS.POST.CODE}")
	private String postCode;

	/** 岗位名称 */
	@ExcelProperty("{ENT.SYS.POST.NAME}")
	private String postName;

	/** 岗位排序 */
	@ExcelProperty("{CC.ENTITY.SORT}")
	private Integer postSort;

	/** 状态（0正常 1停用） */
	@ExcelProperty("{CC.ENTITY.STATUS}")
	private String status;

	/** 用户是否存在此岗位标识 默认不存在 */
	@ExcelIgnore
	@TableField(exist = false)
	private boolean flag = false;
}
