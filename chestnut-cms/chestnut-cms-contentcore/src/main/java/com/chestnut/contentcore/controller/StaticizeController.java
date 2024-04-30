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
package com.chestnut.contentcore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.staticize.func.IFunction;
import com.chestnut.common.staticize.tag.ITag;
import com.chestnut.contentcore.domain.vo.TemplateFuncVO;
import com.chestnut.contentcore.domain.vo.TemplateTagVO;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.system.security.AdminUserType;

import lombok.RequiredArgsConstructor;

/**
 * 静态化管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.StaticizeView)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/staticize")
public class StaticizeController extends BaseRestController {

	private final List<ITag> tags;

	private final List<IFunction> functions;

	/**
	 * 获取静态化自定义模板标签列表
	 */
	@GetMapping("/tags")
	public R<?> getTemplateTags() {
		List<TemplateTagVO> list = this.tags.stream().map(tag -> {
			TemplateTagVO vo = TemplateTagVO.builder().name(I18nUtils.get(tag.getName())).tagName(tag.getTagName())
					.description(I18nUtils.get(tag.getDescription())).tagAttrs(tag.getTagAttrs()).build();
			vo.getTagAttrs().forEach(attr -> {
				attr.setName(I18nUtils.get(attr.getName()));
				attr.setUsage(I18nUtils.get(attr.getUsage()));
			});
			return vo;
		}).toList();
		return R.ok(list);
	}

	/**
	 * 获取静态化自定义模板函数列表
	 */
	@GetMapping("/functions")
	public R<?> getTemplateFunctions() {
		List<TemplateFuncVO> list = this.functions.stream().map(func -> {
			TemplateFuncVO vo = TemplateFuncVO.builder().funcName(func.getFuncName())
					.desc(I18nUtils.get(func.getDesc())).funcArgs(func.getFuncArgs()).build();
			vo.getFuncArgs().forEach(arg -> {
				arg.setName(I18nUtils.get(arg.getName()));
				arg.setDesc(I18nUtils.get(arg.getDesc()));
			});
			vo.setAliases(func.getAliases());
			return vo;
		}).toList();
		return R.ok(list);
	}
}
