package com.chestnut.common.extend.xss;

import java.util.Objects;

public class XssContextHolder {

	private static final ThreadLocal<Boolean> CONTEXT = new ThreadLocal<>();
	
	/**
	 * 默认为true
	 */
	public static boolean isIgnore() {
		return Objects.isNull(CONTEXT.get()) ? true : CONTEXT.get();
	}
	
	public static void ignore() {
		CONTEXT.set(true);
	}
	
	public static void remove() {
		CONTEXT.remove();
	}
}
