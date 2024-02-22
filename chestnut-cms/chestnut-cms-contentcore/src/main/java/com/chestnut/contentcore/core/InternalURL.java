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
package com.chestnut.contentcore.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.util.HtmlUtils;

import com.chestnut.common.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部数据自定义URL<br/>
 * 内部数据包括：内容、栏目、站点、资源等<br/>
 * 例如：<br/>
 * 站点：iurl://site?id=123<br/>
 * 栏目：iurl://catalog/?id=123<br/>
 * 内容：iurl://content?id=123<br/>
 * 资源：iurl://resources/image/2021/12/15/123.png?type=resource&id=123<br/>
 * 页面部件：iurl://pagewidget?id=123<br/>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class InternalURL {

	/**
	 * 链接前缀
	 */
	public static final String IURLProtocol = "iurl://";

	/**
	 * 内部数据类型<br/>
	 * 
	 * @see com.chestnut.contentcore.core.IInternalDataType
	 */
	private String type;

	/**
	 * 数据ID
	 */
	private Long id;

	/**
	 * 其他自定义参数
	 */
	private Map<String, String> params;

	/**
	 * 相对路径，style=Path时有效
	 */
	private String path;

	public InternalURL() {
	}

	public InternalURL(String type, Long id) {
		this.type = type;
		this.id = id;
	}

	public InternalURL(String type, Long id, String path) {
		this.type = type;
		this.id = id;
		this.path = path;
	}

	public InternalURL(String type, Long id, String path, Map<String, String> params) {
		this.type = type;
		this.id = id;
		this.path = path;
		this.params = params;
	}

	public Map<String, String> addParam(String key, String value) {
		if (this.params == null) {
			this.params = new HashMap<>();
		}
		if (key.equals("id")) {
			throw new RuntimeException("内部链接自定义参数不可使用固定参数：id");
		}
		this.params.put(key, value);
		return this.params;
	}

	public static InternalURL parse(String url) {
		InternalURL iurl = new InternalURL();
		url = HtmlUtils.htmlUnescape(url);
		String content = url.substring(IURLProtocol.length());
		int i = content.lastIndexOf("?");
		// 默认iurl的路径部分就是内部数据类型，如果参数中含有type则使用参数type，路径部分作为path
		String type = content.substring(0, i);
		iurl.setType(type);

		String params = content.substring(i + 1);
		Map<String, String> args = StringUtils.splitToMap(params, "&", "=");
		if (args.containsKey("type")) {
			iurl.setPath(iurl.getType());
			iurl.setType(args.get("type"));
			args.remove("type");
		}
		iurl.setId(Long.valueOf(args.get("id")));
		args.remove("id");
		// 自定义参数
		iurl.setParams(args);
		return iurl;
	}

	public String toIUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(IURLProtocol);
		if (StringUtils.isNotEmpty(this.getPath())) {
			sb.append(this.getPath()).append("?type=").append(this.getType()).append("&id=").append(this.getId());
		} else {
			sb.append(this.getType()).append("?id=").append(this.getId());
		}
		if (Objects.nonNull(this.getParams()) && !this.getParams().isEmpty()) {
			this.getParams().forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
		}
		return sb.toString();
	}
}