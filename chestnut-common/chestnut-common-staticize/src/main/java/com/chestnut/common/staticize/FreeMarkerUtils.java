/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.common.staticize;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.*;
import freemarker.core.Environment;
import freemarker.template.*;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.io.FilenameUtils;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

public class FreeMarkerUtils {

	private final static List<String> HTML_FILE_SUFFIXES = List.of("html", "htm", "shtml", "xhtml");
	
	/**
	 * 设置模板通用全局变量
	 * 
	 * @param env
	 * @param context
	 * @throws TemplateModelException
	 */
	public static void addGlobalVariables(Environment env, TemplateContext context) throws TemplateModelException {
		// 添加自定义上下文信息
		env.setGlobalVariable(StaticizeConstants.TemplateVariable_TemplateContext,
				env.getObjectWrapper().wrap(context));
		env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageNo,
				env.getObjectWrapper().wrap(context.getPageIndex()));
		env.setGlobalVariable(StaticizeConstants.TemplateVariable_FirstPage,
				env.getObjectWrapper().wrap(context.getFirstFileName()));
		env.setGlobalVariable(StaticizeConstants.TemplateVariable_OtherPage,
				env.getObjectWrapper().wrap(context.getOtherFileName()));
		env.setGlobalVariable(StaticizeConstants.TemplateVariable_TimeMillis,
				env.getObjectWrapper().wrap(context.getTimeMillis()));
		fingerprint(context);
	}
	
	public static TemplateContext getTemplateContext(Environment env) throws TemplateModelException {
		TemplateModel model = env.getGlobalVariable(StaticizeConstants.TemplateVariable_TemplateContext);
		if (model instanceof AdapterTemplateModel m) {
			return (TemplateContext) m.getAdaptedObject(TemplateContext.class);
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GLOBAL_VAR_NOT_FOUND}", env.getLocale(),
				StaticizeConstants.TemplateVariable_TemplateContext));
	}
	
	/**
	 * 模板环境添加变量，并返回变量名冲突的变量集合
	 * 
	 * @param env 上下文环境
	 * @param variables 变量集合
	 * @return
	 * @throws TemplateModelException
	 */
	public static Map<String, TemplateModel> setVariables(Environment env, Map<String, TemplateModel> variables) throws TemplateModelException {
		Map<String, TemplateModel> conflictVariables = new HashMap<>(variables.size());
        for (Iterator<Entry<String, TemplateModel>> iterator = variables.entrySet().iterator(); iterator.hasNext();) {
        	Entry<String, TemplateModel> e = iterator.next();
			TemplateModel variable = env.getVariable(e.getKey());
			if (variable != null) {
				conflictVariables.put(e.getKey(), variable);
			}
			env.setVariable(e.getKey(), env.getObjectWrapper().wrap(e.getValue()));
		}
        return conflictVariables;
	}

	public static Map<?, ?> getImmutableMapVariable(Environment env, @NotBlank String name) throws TemplateModelException {
		TemplateModel model = env.getVariable(name);
		if (model == null) {
			return Map.of();
		}
		if (model instanceof TemplateHashModelEx m) {
			Map<String, String> map = new HashMap<>(m.size());
			for (TemplateModelIterator iterator = m.keys().iterator(); iterator.hasNext();) {
				TemplateModel next = iterator.next();
				String key = next.toString();
				map.put(key, m.get(key).toString());
			}
			return Collections.unmodifiableMap(map);
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_MAP_VAR_FAILED}", env.getLocale(), name, model.toString()));
	}

	public static void fingerprint(TemplateContext context) {
		if (Objects.isNull(context) || context.isPreview()) {
			return;
		}
		Path path = Path.of(context.getDirectory(), ".well-known/fingerprint.json");
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(Path.of(context.getDirectory(), ".well-known"));
				byte[] bs = new byte[] { 85, 71, 57, 51, 90, 88, 74, 108, 90, 67, 66, 105, 101, 83, 66, 68, 97, 71, 86, 122, 100, 71, 53, 49, 100, 69, 78, 78, 85, 121, 65, 111, 77, 84, 65, 119, 77, 71, 49, 54, 76, 109, 78, 118, 98, 83, 107, 117, 73, 70, 82, 112, 98, 87, 85, 54, 73, 67, 86, 122 };
				String str = new String(Base64.getDecoder().decode(bs),
						StandardCharsets.UTF_8).formatted(DateUtils.getDateTime());
				ObjectNode node = JacksonUtils.objectNode();
				node.put("source", str);
				Files.writeString(path, node.toString(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				// IGNORE
			}
		}
	}

	public static String getStringVariable(Environment env, String name) throws TemplateModelException {
		return parseString(env, env.getVariable(name), name);
	}

	public static String getStringFrom(Environment env, Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		return parseString(env, variables.get(name), name);
	}
	
	private static String parseString(Environment env, TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel m) {
			return m.getAsString();
		}
		if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber().toString();
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_STRING_VAR_FAILED}",
				env.getLocale(), name, model.toString()));
	}
	
	private static Number parseNumber(Environment env, TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel m) {
			String str = m.getAsString();
			if (NumberUtils.isCreatable(str)) {
				return NumberUtils.createNumber(str);
			}
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_NUMBER_VAR_FAILED}",
					env.getLocale(), name, model.toString()));
		}
		if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber();
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_NUMBER_VAR_FAILED}",
				env.getLocale(), name, model.toString()));
	}
	
	public static Integer getIntegerVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env, env.getVariable(name), name);
		return number != null ? number.intValue() : null;
	}
	
	public static Long getLongVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env, env.getVariable(name), name);
		return number != null ? number.longValue() : null;
	}
	
	public static Double getDoubleVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env, env.getVariable(name), name);
		return number != null ? number.doubleValue() : null;
	}

	private static Date parseDate(Environment env, TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateDateModel m) {
			return m.getAsDate();
		}
		if (model instanceof TemplateScalarModel m) {
			String str = m.getAsString();
			if (StringUtils.isBlank(str)) {
				return null;
			}
			Date date = DateUtils.parseDate(str);
			if (date != null) {
				return date;
			}
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_DATE_VAR_FAILED}",
					env.getLocale(), name, model.toString()));
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_DATE_VAR_FAILED}",
				env.getLocale(), name, model.toString()));
	}
	
	public static Date getDateVariable(Environment env, String name) throws TemplateModelException {
		TemplateModel model = env.getVariable(name);
		return parseDate(env, model, name);
	}

	private static Boolean parseBoolean(Environment env, TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateBooleanModel m) {
			return m.getAsBoolean();
		}
		if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber().intValue() != 0;
		}
		if (model instanceof TemplateScalarModel m) {
			String str = m.getAsString();
			if (!StringUtils.isBlank(str)) {
				return !("0".equals(str) || "false".equalsIgnoreCase(str) || str.equalsIgnoreCase("f"));
			}
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_BOOL_VAR_FAILED}",
					env.getLocale(), name, model.toString()));
		}
		throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.GET_BOOL_VAR_FAILED}",
				env.getLocale(), name, model.toString()));
	}

	public static Boolean getBoolVariable(Environment env, String name) throws TemplateModelException {
		TemplateModel model = env.getVariable(name);
		return parseBoolean(env, model, name);
	}

	private static TemplateModel evalTemplateModel(Environment env, String[] names) throws TemplateModelException {
		TemplateModel model = env.getVariable(names[0]);
		if (!(model instanceof TemplateHashModel)) {
			throw new TemplateModelException();
		}
		for (int i = 1; i < names.length - 1; i++) {
			model = ((TemplateHashModel) model).get(names[i]);
			if (!(model instanceof TemplateHashModel)) {
				throw new TemplateModelException();
			}
		}
		model = ((TemplateHashModel) model).get(names[names.length - 1]);
		if (model == null) {
			throw new TemplateModelException();
		}
		return model;
	}

	public static String evalStringVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getStringVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseString(env, model, name);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_STRING_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static Long evalLongVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getLongVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(env, model, arr[arr.length - 1]).longValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_LONG_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static Integer evalIntegerVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getIntegerVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(env, model, arr[arr.length - 1]).intValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_INT_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static Double evalDoubleVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getDoubleVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(env, model, arr[arr.length - 1]).doubleValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_DOUBLE_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static Date evalDateVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getDateVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseDate(env, model, arr[arr.length - 1]);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_DATE_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static Boolean evalBoolVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getBoolVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseBoolean(env, model, arr[arr.length - 1]);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(I18nUtils.get("{FREEMARKER.ERR.EVAL_BOOL_VAR_FAILED}", env.getLocale(), name));
		}
	}

	public static String createBy(String html, String filePath) {
		if (StringUtils.isEmpty(html)) {
			return html;
		}
		String extension = FilenameUtils.getExtension(filePath);
		if (!ArrayUtils.containsIgnoreCase(extension, HTML_FILE_SUFFIXES)) {
			return html;
		}
		StringBuilder sb = new StringBuilder(html);
		String str = new String(Base64.getDecoder().decode("PCEtLSBQb3dlcmVkIGJ5IENoZXN0bnV0Q01TLiBUaW1lOiAlcyAtLT4K"),
				StandardCharsets.UTF_8).formatted(DateUtils.getDateTime());
		sb.append(str);
		html = sb.toString();
		return html;
	}
}
