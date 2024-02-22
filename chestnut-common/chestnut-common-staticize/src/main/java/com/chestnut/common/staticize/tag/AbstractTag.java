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
package com.chestnut.common.staticize.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * 自定义标签会作为共享变量添加到Configuration中。非线程安全！！！
 */
public abstract class AbstractTag implements ITag, TemplateDirectiveModel {

	/**
	 * 执行标签逻辑，返回标签变量
	 *
	 * @param env 模板环境
	 * @param attrs 标签属性
	 * @return 标签变量
	 * @throws TemplateException 模板异常
	 * @throws IOException IO异常
	 */
	public abstract Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException;

	@SuppressWarnings({ "unchecked" })
	@Override
	public void execute(Environment env, Map attrs, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		// 校验标签属性
		Map<String, String> tagAttrs = this.validTagAttributes(env, attrs);

		// 执行标签逻辑
		this.onTagStart(env, tagAttrs, body);
		Map<String, TemplateModel> tagVariables = this.execute0(env, tagAttrs);
		this.onTagEnd(env, tagAttrs, body, tagVariables);
	}

	/**
	 * execute0之前执行
	 *
	 * @param env 模板环境
	 * @param tagAttrs 标签属性
	 * @param body 标签内容
	 * @throws TemplateException 模板异常
	 */
	public void onTagStart(Environment env, Map<String, String> tagAttrs, TemplateDirectiveBody body)
			throws TemplateException {
	}

	/**
	 * execute0之后执行
	 *
	 * @param env 模板环境
	 * @param tagAttrs 标签属性
	 * @param body 标签内容
	 * @param tagVariables 标签变量
	 * @throws TemplateException 模板异常
	 * @throws IOException IO异常
	 */
	public void onTagEnd(Environment env, Map<String, String> tagAttrs, TemplateDirectiveBody body,
						 Map<String, TemplateModel> tagVariables) throws TemplateException, IOException {
		if (body != null) {
			if (tagVariables != null) {
				// 保存冲突变量，render后恢复
				Map<String, TemplateModel> conflictVariables = FreeMarkerUtils.setVariables(env, tagVariables);
				body.render(env.getOut());
				tagVariables.forEach((k, v) -> env.setVariable(k, null));
				conflictVariables.forEach(env::setVariable);
			} else {
				body.render(env.getOut());
			}
		}
	}

	/**
	 * 校验标签属性并返回处理过的标签属性
	 *
	 * @param env 模板环境
	 * @param _attrs 标签属性
	 * @throws TemplateException 模板异常
	 */
	Map<String, String> validTagAttributes(Environment env, Map<String, TemplateModel> _attrs) throws TemplateException {
		CaseInsensitiveMap<String, String> map = null;
		List<TagAttr> tagAttrs = this.getTagAttrs();
		if (tagAttrs != null) {
			CaseInsensitiveMap<String, TemplateModel> attrs = new CaseInsensitiveMap<>();
			attrs.putAll(_attrs);
			map = new CaseInsensitiveMap<>();
			for (TagAttr tagAttr : tagAttrs) {
				String attrValue = FreeMarkerUtils.getStringFrom(attrs, tagAttr.getName());
				if (tagAttr.isMandatory() && StringUtils.isEmpty(attrValue)) {
					throw new TemplateException(StringUtils.messageFormat("The tag <@{0}> missing required attribute: {1}", this.getTagName(), tagAttr.getName()),
							env);
				}
				if (tagAttr.getDataType() == TagAttrDataType.INTEGER && StringUtils.isNotEmpty(attrValue)
						&& !NumberUtils.isCreatable(attrValue)) {
					throw new TemplateException(
							StringUtils.messageFormat("The tag <@{0}> attribute `{1}` must be digit, but is: {2}", this.getTagName(), tagAttr.getName(), attrValue),
							env);
				}
				if (tagAttr.getOptions() != null && StringUtils.isNotEmpty(attrValue)) {
					String[] optionValues = tagAttr.getOptions().stream().map(TagAttrOption::lowerCaseValue).toArray(String[]::new);
					if (!ArrayUtils.contains(optionValues, attrValue.toLowerCase())) {
						throw new TemplateException(StringUtils.messageFormat("The tag <@{0}> attribute `{1}` = `{2}` is invalid, options: {3}", this.getTagName(),
								tagAttr.getName(), attrValue, Arrays.toString(optionValues)), env);
					}
				}
				if (StringUtils.isEmpty(attrValue) && StringUtils.isNotEmpty(tagAttr.getDefaultValue())) {
					attrValue = tagAttr.getDefaultValue();
				}
				map.put(tagAttr.getName(), attrValue);
			}
		}
		return map;
	}

	/**
	 * 构造模板变量
	 *
	 * @param env 模板环境
	 * @param o 变量值
	 * @return 模板变量
	 * @throws TemplateModelException 模板异常
	 */
	public TemplateModel wrap(Environment env, Object o) throws TemplateModelException {
		return env.getObjectWrapper().wrap(o);
	}
}
