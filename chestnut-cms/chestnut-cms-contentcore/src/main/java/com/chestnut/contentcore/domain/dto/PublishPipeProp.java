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
package com.chestnut.contentcore.domain.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PublishPipeProp {

	private String pipeCode;
	
	private String pipeName;
	
	private Map<String, Object> props;
	
	public static PublishPipeProp newInstance(String pipeCode, String pipeName, Map<String, Object> props) {
		PublishPipeProp ppp = new PublishPipeProp();
		ppp.setPipeCode(pipeCode);
		ppp.setPipeName(pipeName);
		ppp.setProps(Objects.requireNonNullElse(props, new HashMap<>()));
		return ppp;
	}

	public String getPipeCode() {
		return pipeCode;
	}

	public void setPipeCode(String pipeCode) {
		this.pipeCode = pipeCode;
	}

	public String getPipeName() {
		return pipeName;
	}

	public void setPipeName(String pipeName) {
		this.pipeName = pipeName;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> prop) {
		this.props = prop;
	}
}
