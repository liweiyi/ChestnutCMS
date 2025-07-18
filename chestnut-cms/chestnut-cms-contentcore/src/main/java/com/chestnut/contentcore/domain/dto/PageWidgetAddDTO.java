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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class PageWidgetAddDTO {

	/**
	 * 栏目ID
	 */
	@NotNull
	@Min(0)
	private Long catalogId;

	/**
	 * 页面部件类型
	 */
	@NotEmpty
	private String type;

	/**
	 * 名称
	 */
	@NotEmpty
	private String name;

	/**
	 * 编码
	 */
	@NotEmpty
	private String code;

	/**
	 * 发布通道编码
	 */
	@Deprecated(since = "1.5.6", forRemoval = true)
	private String publishPipeCode;

	/**
	 * 模板路径
	 */
	@Deprecated(since = "1.5.6", forRemoval = true)
	private String template;

	/**
	 * 发布通道模板配置
	 */
	private List<PublishPipeTemplate> templates;

	/**
	 * 静态化目录
	 */
	private String path;
    
	/**
	 * 备注
	 */
    private String remark;

	public Map<String, String> getPublishPipeTemplateMap() {
		if (StringUtils.isEmpty(this.templates)) {
			return Map.of();
		}
		return this.templates.stream().collect(Collectors.toMap(PublishPipeTemplate::code, PublishPipeTemplate::template));
	}
}
