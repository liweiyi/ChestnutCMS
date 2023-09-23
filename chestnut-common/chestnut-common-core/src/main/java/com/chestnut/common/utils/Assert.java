package com.chestnut.common.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

public class Assert {
	
	/**
	 * 断言对象不为null
	 * 
	 * @param object
	 * @param errorSupplier
	 * @throws X
	 */
	public static <T, X extends Throwable> T notNull(T object, Supplier<X> errorSupplier) throws X {
		if (Objects.isNull(object)) {
			throw errorSupplier.get();
		}
		return object;
	}

	/**
	 * 断言对象为null
	 * 
	 * @param object
	 * @param errorSupplier
	 * @throws X
	 */
	public static <T, X extends Throwable> T isNull(T object, Supplier<X> errorSupplier) throws X {
		if (Objects.nonNull(object)) {
			throw errorSupplier.get();
		}
		return object;
	}
	
	/**
	 * 断言字符串不为null且长度大于0
	 * 
	 * @param object
	 * @param errorSupplier
	 * @throws X
	 */
	public static <T extends CharSequence, X extends Throwable> T notEmpty(T text, Supplier<X> errorSupplier) throws X {
		if (StringUtils.isEmpty(text)) {
			throw errorSupplier.get();
		}
		return text;
	}
	
	/**
	 * 断言集合不为空
	 * 
	 * @param collection
	 * @param errorSupplier
	 * @throws X
	 */
	public static <E, T extends Collection<E>, X extends Throwable> T notEmpty(T collection, Supplier<X> errorSupplier) throws X {
		if (CollectionUtils.isEmpty(collection)) {
			throw errorSupplier.get();
		}
		return collection;
	}
	
	/**
	 * 断言数组不为null切长度大于0
	 * 
	 * @param array
	 * @param errorSupplier
	 * @throws X
	 */
	public static <T, X extends Throwable> T[] notEmpty(T[] array, Supplier<X> errorSupplier) throws X {
		if (ArrayUtils.isEmpty(array)) {
			throw errorSupplier.get();
		}
		return array;
	}

	/**
	 * 断言为true
	 * 
	 * @param expression
	 * @param errorSupplier
	 * @throws X
	 */
	public static <X extends Throwable> void isTrue(boolean expression, Supplier<? extends X> errorSupplier) throws X {
		if (false == expression) {
			throw errorSupplier.get();
		}
	}

	/**
	 * 断言为false
	 * 
	 * @param expression
	 * @param errorSupplier
	 * @throws X
	 */
	public static <X extends Throwable> void isFalse(boolean expression, Supplier<? extends X> errorSupplier) throws X {
		if (true == expression) {
			throw errorSupplier.get();
		}
	}

	/**
	 * 断言num大于target
	 * 
	 * @param str
	 * @param errorSupplier
	 * @throws X
	 */
	public static <X extends Throwable> void gtThan(int num, int target, Supplier<X> errorSupplier) throws X {
		if (num <= target) {
			throw errorSupplier.get();
		}
	}

	/**
	 * 断言num小于target
	 * 
	 * @param str
	 * @param errorSupplier
	 * @throws X
	 */
	public static <X extends Throwable> void ltThan(int num, int target, Supplier<X> errorSupplier) throws X {
		if (num >= target) {
			throw errorSupplier.get();
		}
	}
}
