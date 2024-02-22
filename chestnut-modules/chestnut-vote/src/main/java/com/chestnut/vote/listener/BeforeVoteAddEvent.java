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
package com.chestnut.vote.listener;

import org.springframework.context.ApplicationEvent;

import com.chestnut.vote.domain.Vote;

import lombok.Getter;

@Getter
public class BeforeVoteAddEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private Vote vote;
	
	public BeforeVoteAddEvent(Object source, Vote vote) {
		super(source);
		this.vote = vote;
	}
}
