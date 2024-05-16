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
package com.chestnut.common.staticize;

import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FreeMarkerUtils {
	
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
	}
	
	public static TemplateContext getTemplateContext(Environment env) throws TemplateModelException {
		TemplateModel model = env.getGlobalVariable(StaticizeConstants.TemplateVariable_TemplateContext);
		if (model instanceof AdapterTemplateModel m) {
			return (TemplateContext) m.getAdaptedObject(TemplateContext.class);
		}
		throw new TemplateModelException(StringUtils.messageFormat("Global variable `{0}` not found.", StaticizeConstants.TemplateVariable_TemplateContext));
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

	public static String getStringVariable(Environment env, String name) throws TemplateModelException {
		return parseString(env.getVariable(name), name);
	}

	public static String getStringFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		return parseString(variables.get(name), name);
	}
	
	private static String parseString(TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel m) {
			return m.getAsString();
		} else if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber().toString();
		}
		throw new TemplateModelException(StringUtils.messageFormat("Get string variable failed: {0} = {1}", name, model.toString()));
	}
	
	private static Number parseNumber(TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel m) {
			String str = m.getAsString();
			if (NumberUtils.isCreatable(str)) {
				return NumberUtils.createNumber(str);
			}
		} else if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber();
		}
		throw new TemplateModelException(StringUtils.messageFormat("Get number variable failed: {0} = {1}", name, model.toString()));
	}
	
	public static Integer getIntegerVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env.getVariable(name), name);
		return number != null ? number.intValue() : null;
	}
	
	public static Integer getIntegerFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		Number number = parseNumber(variables.get(name), name);
		return number != null ? number.intValue() : null;
	}
	
	public static Long getLongVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env.getVariable(name), name);
		return number != null ? number.longValue() : null;
	}
	
	public static Long getLongFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		Number number = parseNumber(variables.get(name), name);
		return number != null ? number.longValue() : null;
	}
	
	public static Double getDoubleVariable(Environment env, String name) throws TemplateModelException {
		Number number = parseNumber(env.getVariable(name), name);
		return number != null ? number.doubleValue() : null;
	}
	
	public static Double getDoubleFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		Number number = parseNumber(variables.get(name), name);
		return number != null ? number.doubleValue() : null;
	}
	
	private static Date parseDate(TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateDateModel m) {
			return m.getAsDate();
		} else if (model instanceof TemplateScalarModel m) {
			String str = m.getAsString();
			if (StringUtils.isBlank(str)) {
				return null;
			}
			Date date = DateUtils.parseDate(str);
			if (date != null) {
				return date;
			}
		}
		throw new TemplateModelException(StringUtils.messageFormat("Get date env variable failed: {0} = {1}", name, model.toString()));
	}
	
	public static Date getDateVariable(Environment env, String name) throws TemplateModelException {
		return parseDate(env.getVariable(name), name);
	}
	
	public static Date getDateFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		return parseDate(variables.get(name), name);
	}
	
	private static Boolean parseBoolean(TemplateModel model, String name) throws TemplateModelException {
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateBooleanModel m) {
			return m.getAsBoolean();
		} else if (model instanceof TemplateNumberModel m) {
			return m.getAsNumber().intValue() != 0;
		} else if (model instanceof TemplateScalarModel m) {
			String s = m.getAsString();
			if (!StringUtils.isBlank(s)) {
				return !("0".equals(s) || "false".equalsIgnoreCase(s) || s.equalsIgnoreCase("f"));
			}
		}
		throw new TemplateModelException(StringUtils.messageFormat("Get boolean env variable failed: {0} = {1}", name, model.toString()));
	}

	public static Boolean getBoolVariable(Environment env, String name) throws TemplateModelException {
		return parseBoolean(env.getVariable(name), name);
	}
	
	public static Boolean getBoolFrom(Map<String, TemplateModel> variables, String name) throws TemplateModelException {
		return parseBoolean(variables.get(name), name);
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
			return parseString(model, name);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval string env variable failed: {0}", name));
		}
	}

	public static Long evalLongVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getLongVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(model, arr[arr.length - 1]).longValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval long env variable failed: {0}", name));
		}
	}

	public static Integer evalIntegerVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getIntegerVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(model, arr[arr.length - 1]).intValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval integer env variable failed: {0}", name));
		}
	}

	public static Double evalDoubleVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getDoubleVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseNumber(model, arr[arr.length - 1]).doubleValue();
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval double env variable failed: {0}", name));
		}
	}

	public static Date evalDateVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getDateVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseDate(model, arr[arr.length - 1]);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval date env variable failed: {0}", name));
		}
	}

	public static Boolean evalBoolVariable(Environment env, String name) throws TemplateModelException {
		String[] arr = StringUtils.split(name, StringUtils.DOT);
		if (arr.length == 1) {
			return getBoolVariable(env, name);
		}
		try {
			TemplateModel model = evalTemplateModel(env, arr);
			return parseBoolean(model, arr[arr.length - 1]);
		} catch (TemplateModelException e) {
			throw new TemplateModelException(StringUtils.messageFormat("Eval date boolean variable failed: {0}", name));
		}
	}
}
