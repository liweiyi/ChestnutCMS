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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 更新补丁记录表 sys_update_patcher
 */
@Getter
@Setter
@XComment("{ENT.SYS.UPDATE_PATCHER.ENTITY}")
@TableName(SysUpdatePatcher.TABLE_NAME)
public class SysUpdatePatcher implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_update_patcher";

	@XComment("{ENT.SYS.UPDATE_PATCHER.ID}")
	@TableId(value = "patcher_id", type = IdType.INPUT)
	private String patcherId;

	/**
	 * 执行开始时间
	 */
	@XComment("{ENT.SYS.UPDATE_PATCHER.START_TIME}")
	private Instant startTime;

	/**
	 * 耗时，单位：秒
	 */
	@XComment("{ENT.SYS.UPDATE_PATCHER.COST_SECONDS}")
	private Integer costSeconds;
}
