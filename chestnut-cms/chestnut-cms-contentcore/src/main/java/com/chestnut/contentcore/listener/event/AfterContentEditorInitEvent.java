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
package com.chestnut.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.domain.vo.ContentVO;

import lombok.Getter;

@Getter
public class AfterContentEditorInitEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private ContentVO contentVO;
	
	public AfterContentEditorInitEvent(Object source, ContentVO vo) {
		super(source);
		this.contentVO = vo;
	}
}
