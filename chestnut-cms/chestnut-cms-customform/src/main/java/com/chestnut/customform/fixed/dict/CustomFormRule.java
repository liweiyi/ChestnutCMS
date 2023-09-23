package com.chestnut.customform.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 自定义表单提交限制规则
 */
@Component(FixedDictType.BEAN_PREFIX + CustomFormRule.TYPE)
public class CustomFormRule extends FixedDictType {

	public static final String TYPE = "CustomFormRule";

	public static final String Unlimited = "0"; // 无限制

	public static final String IP = "1"; // IP

	public static final String BrowserFingerprint = "2"; // 浏览器指纹


	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public CustomFormRule() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + Unlimited + "}", Unlimited, 1);
		super.addDictData("{DICT." + TYPE + "." + IP + "}", IP, 2);
		super.addDictData("{DICT." + TYPE + "." + BrowserFingerprint + "}", BrowserFingerprint, 3);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
