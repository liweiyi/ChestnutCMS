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
package com.chestnut.vote.core.impl;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.ServletUtils;
import com.chestnut.vote.core.IVoteUserType;

@Component(IVoteUserType.BEAN_PREFIX + IPVoteUserType.ID)
public class IPVoteUserType implements IVoteUserType {

	public static final String ID = "ip";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "{VOTE.USER_TYPE.IP}";
	}

	@Override
	public String getUserId() {
		return ServletUtils.getIpAddr(ServletUtils.getRequest());
	}
}
