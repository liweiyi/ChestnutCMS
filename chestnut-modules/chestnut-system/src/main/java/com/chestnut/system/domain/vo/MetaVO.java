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
package com.chestnut.system.domain.vo;

import com.chestnut.common.utils.ServletUtils;

/**
 * 路由显示信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MetaVO {
	/**
	 * 设置该路由在侧边栏和面包屑中展示的名字
	 */
	private String title;

	/**
	 * 设置该路由的图标，对应路径src/assets/icons/svg
	 */
	private String icon;

	/**
	 * 设置为true，则不会被 <keep-alive>缓存
	 */
	private boolean noCache;

	/**
	 * 内链地址（http(s)://开头）
	 */
	private String link;

	public MetaVO() {
	}

	public MetaVO(String title, String icon) {
		this.title = title;
		this.icon = icon;
	}

	public MetaVO(String title, String icon, boolean noCache) {
		this.title = title;
		this.icon = icon;
		this.noCache = noCache;
	}

	public MetaVO(String title, String icon, String link) {
		this.title = title;
		this.icon = icon;
		this.link = link;
	}

	public MetaVO(String title, String icon, boolean noCache, String link) {
		this.title = title;
		this.icon = icon;
		this.noCache = noCache;
		if (ServletUtils.isHttpUrl(link)) {
			this.link = link;
		}
	}

	public boolean isNoCache() {
		return noCache;
	}

	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
