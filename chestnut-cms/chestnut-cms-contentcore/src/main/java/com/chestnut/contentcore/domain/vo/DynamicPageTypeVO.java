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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.core.IDynamicPageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DynamicPageTypeVO {

	private String type;
	
	private String name;
	
	private String desc;
	
	private String requestPath;

	private String publishPipeKey;
	
	private List<IDynamicPageType.RequestArg> requestArgs;

	public static DynamicPageTypeVO newInstance(IDynamicPageType dynamicPageType) {
		DynamicPageTypeVO vo = new DynamicPageTypeVO();
		vo.setType(dynamicPageType.getType());
		vo.setName(dynamicPageType.getName());
		vo.setDesc(dynamicPageType.getDesc());
		vo.setRequestPath(dynamicPageType.getRequestPath());
		vo.setPublishPipeKey(dynamicPageType.getPublishPipeKey());
		vo.setRequestArgs(dynamicPageType.getRequestArgs());
		return vo;
	}
}
