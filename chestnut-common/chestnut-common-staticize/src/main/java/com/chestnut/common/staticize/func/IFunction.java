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
package com.chestnut.common.staticize.func;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public interface IFunction {
	
	/**
	 * 函数使用名<br/>
	 * 例如：${replace(txt, 'findStr', 'repStr')}
	 * 
	 * @return
	 */
	String getFuncName();
	
	/**
	 * 描述
	 * 
	 * @return
	 */
	String getDesc();
	
	/**
	 * 获取函数参数定义列表
	 * 
	 * @return
	 */
	default List<FuncArg> getFuncArgs() {
		return List.of();
	}

	/**
	 * 函数别名
	 */
	default List<String> getAliases() {
		return List.of();
	}

	/**
	 * 模板函数参数
	 */
	@Getter
	@Setter
	class FuncArg {
		
		private String name;
		
		private FuncArgType type;
		
		private boolean required;
		
		private String desc;
		
		public FuncArg(String name, FuncArgType type, boolean required, String desc) {
			this.name = name;
			this.type = type;
			this.required = required;
			this.desc = desc;
		}
	}
	
	enum FuncArgType {
		String, Int, Long, Float, Double, DateTime, Boolean
	}
}
