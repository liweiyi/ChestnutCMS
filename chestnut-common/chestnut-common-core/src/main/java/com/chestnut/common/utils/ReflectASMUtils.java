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
package com.chestnut.common.utils;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ReflectASMUtils {

	private final static HashMap<String, FieldAccess> FieldAccessMap = new HashMap<>();

	private final static HashMap<String, MethodAccess> MethodAccessMap = new HashMap<>();
	
	private static FieldAccess getFieldAccess(Class<?> clazz) {
		FieldAccess fa = FieldAccessMap.get(clazz.getTypeName());
		if (Objects.isNull(fa)) {
			fa = FieldAccess.get(clazz);
			FieldAccessMap.put(clazz.getTypeName(), fa);
		}
		return fa;
	}
	
	/**
	 * 获取public属性值
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static <T> T getFieldValue(Object obj, String fieldName, Class<T> returnType) {
		try {
			if (ObjectUtils.isAnyNull(obj, fieldName, returnType) || fieldName.isBlank()) {
				throw new IllegalArgumentException("The parameters obj/fieldName/returnType cannot be null and fieldName not blank.");
			}
			FieldAccess fieldAccess = getFieldAccess(obj.getClass());
			return returnType.cast(fieldAccess.get(obj, fieldName));
		} catch (Exception e) {
			log.error("Get field `{}.{}` value failed.", obj.getClass().getName(), fieldName, e);
			return null;
		}
	}
	
	/**
	 * 设置public属性值
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static void setFieldValue(Object obj, String fieldName, Object fieldValue) {
		try {
			if (ObjectUtils.isAnyNull(obj, fieldName) || fieldName.isBlank()) {
				throw new IllegalArgumentException("The  parameters obj/fieldName cannot be null and fieldName not blank.");
			}
			FieldAccess fieldAccess = getFieldAccess(obj.getClass());
			fieldAccess.set(obj, fieldName, fieldValue);
		} catch (Exception e) {
			log.error("Set value to field `{}.{}` failed.", obj.getClass().getName(), fieldName, e);
		}
	}

	private static synchronized MethodAccess getMethodAccess(Class<?> clazz) {
		MethodAccess ma = MethodAccessMap.get(clazz.getTypeName());
		if (Objects.isNull(ma)) {
			ma = MethodAccess.get(clazz);
			MethodAccessMap.put(clazz.getTypeName(), ma);
		}
		return ma;
	}
	
	public static Map<String, Object> beanToMap(Object object) {
		Map<String, Object> map = new HashMap<>();
		MethodAccess methodAccess = getMethodAccess(object.getClass());
		String[] methodNames = methodAccess.getMethodNames();
		for (String methodName : methodNames) {
			if (methodName.length() > 3 && methodName.startsWith("get")) {
				int index = methodAccess.getIndex(methodName);
				if (methodAccess.getParameterTypes()[index].length == 0) {
					// 仅处理参数数量为0的get方法
					String fieldName = StringUtils.lowerFirst(methodName.substring(3));
					Object v = methodAccess.invoke(object, index);
					map.put(fieldName, v);
				}
			}
		}
		return map;
	}
	
	public static <T> T invokeMethod(Object obj, String method, Class<T> returnType, Object... params) {
		try {
			if (ObjectUtils.isAnyNull(obj, method, returnType) || method.isBlank()) {
				throw new IllegalArgumentException("The parameters obj/method/returnType cannot be null and method not blank.");
			}
			MethodAccess ma = getMethodAccess(obj.getClass());
			return returnType.cast(ma.invoke(obj, method, params));
		} catch (Exception e) {
			log.error("Invoke method `{}.{}` failed.", obj.getClass().getName(), method, e);
			return null;
		}
	}

	public static void invokeSetter(Object obj, String fieldName, Object value) {
		try {
			if (ObjectUtils.isAnyNull(obj, fieldName) || fieldName.isBlank()) {
				throw new IllegalArgumentException("The parameters obj/fieldName cannot be null and fieldName not blank.");
			}
			MethodAccess ma = getMethodAccess(obj.getClass());
			String methodName = "set" + StringUtils.upperFirst(fieldName);
			ma.invoke(obj, methodName, value);
		} catch (Exception e) {
			log.error("Invoke setter method `{}.{}` failed.", obj.getClass().getName(), fieldName, e);
		}
	}

	public static <T> T invokeGetter(Object obj, String fieldName, Class<T> returnType) {
		try {
			if (ObjectUtils.isAnyNull(obj, returnType, fieldName) || fieldName.isBlank()) {
				throw new IllegalArgumentException("The parameters obj/fieldName/returnType cannot be null and fieldName not blank.");
			}
			MethodAccess ma = getMethodAccess(obj.getClass());
			String methodName = "get" + StringUtils.upperFirst(fieldName);
			Object v = ma.invoke(obj, methodName);
			return returnType.cast(v);
		} catch (Exception e) {
			log.error("Invoke getter method `{}.{}` failed.", obj.getClass().getName(), fieldName, e);
			return null;
		}
	}

	public static Object invokeGetter(Object obj, String fieldName) {
		try {
			if (ObjectUtils.isAnyNull(obj, fieldName) || fieldName.isBlank()) {
				throw new IllegalArgumentException("The parameters obj/fieldName cannot be null and fieldName not blank.");
			}
			MethodAccess ma = getMethodAccess(obj.getClass());
			String methodName = "get" + StringUtils.upperFirst(fieldName);
			return ma.invoke(obj, methodName);
		} catch (Exception e) {
			log.error("Invoke getter method `{}.{}` failed.", obj.getClass().getName(), fieldName, e);
			return null;
		}
	}
}
