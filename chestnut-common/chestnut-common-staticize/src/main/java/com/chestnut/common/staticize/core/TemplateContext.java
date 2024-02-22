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
package com.chestnut.common.staticize.core;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板上下文
 */
@Getter
@Setter
public class TemplateContext {
	
	public static final String PlaceHolder_PageNo = "{0}";
	
	/**
	 * 模板唯一标识，相对ResourceRoot的模板路径
	 */
	private String templateId;
	
	/**
	 * 模板初始变量globalVariables
	 */
	private Map<String, Object> variables = new HashMap<>();
	
	/**
	 * 第一页静态化文件名
	 */
	private String firstFileName;
	
	/**
	 * 其他页静态化文件名，栏目列表或文章分页时会用到<br/>
	 * 参数：{0} = 当前页码
	 * 例如：list_{0}.shtml
	 */
	private String otherFileName;
	
	/**
	 * 静态化文件生成目录
	 */
	private String directory;

	/**
	 * 是否预览状态，默认false
	 */
	private boolean preview;
	
	/**
	 * 发布通道编码
	 */
	private String publishPipeCode;
	
	/**
	 * 当前页码从1开始
	 */
	private int pageIndex = 1;
	
	/**
	 * 分页数
	 */
	private int pageSize;
	
	/**
	 * 总数据量
	 */
	private long pageTotal;
	
	/**
	 * 最大静态化页数
	 */
	private int maxPageNo;
	
	/**
	 * 是否已激活分页标识<br/>
	 * 注意：需要生成多页面的模板中只能有一个page=true的列表标签，存在多个会覆盖掉分页属性
	 */
	private boolean paged;

	/**
	 * 模板处理时间
	 */
	private Long timeMillis;
	
	public TemplateContext(String templateId, boolean preview, String publishPipeCode) {
		this.templateId = templateId;
		this.preview = preview;
		this.publishPipeCode = publishPipeCode;
	}

	public String getStaticizeFilePath(int pageNo) {
		if (pageNo <= 1) {
			return this.getDirectory() + this.getFirstFileName();
		}
		return this.getDirectory() + StringUtils.messageFormat(this.getOtherFileName(), pageNo);
	}

	public boolean hasNextPage() {
		if (this.getPageSize() > 0 && StringUtils.isNotEmpty(this.getOtherFileName())) {
			if (this.getMaxPageNo() > 0 && this.getPageIndex() >= this.getMaxPageNo()) {
				return false;
			}
			return (long) this.getPageIndex() * getPageSize() < getPageTotal();
		}
		return false;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = Math.max(pageIndex, 1);
		this.paged = false; // 变更页码时重置分页激活标记
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		FileExUtils.mkdirs(directory);
	}
}
