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
package com.chestnut.system.domain.vo.server;

import com.chestnut.common.utils.Arith;
import lombok.Getter;
import lombok.Setter;

/**
 * CPU相关信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class Cpu {
	
	/**
	 * 核心数
	 */
	private int cpuNum;

	/**
	 * CPU总的使用率
	 */
	private double total;

	/**
	 * CPU系统使用率
	 */
	private double sys;

	/**
	 * CPU用户使用率
	 */
	private double used;

	/**
	 * CPU当前等待率
	 */
	private double wait;

	/**
	 * CPU当前空闲率
	 */
	private double free;

	public double getTotal() {
		return Arith.round(Arith.mul(total, 100), 2);
	}

	public double getSys() {
		return Arith.round(Arith.mul(sys / total, 100), 2);
	}

	public double getUsed() {
		return Arith.round(Arith.mul(used / total, 100), 2);
	}

	public double getWait() {
		return Arith.round(Arith.mul(wait / total, 100), 2);
	}

	public double getFree() {
		return Arith.round(Arith.mul(free / total, 100), 2);
	}
}
