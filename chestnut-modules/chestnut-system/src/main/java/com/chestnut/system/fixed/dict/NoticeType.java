package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 通知类型
 */
@Component(FixedDictType.BEAN_PREFIX + NoticeType.TYPE)
public class NoticeType extends FixedDictType {

	public static final String TYPE = "NoticeType";

	public static final String NOTICE = "1"; // 通知
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public NoticeType() {
		super(TYPE, "{DICT." + TYPE + "}", true);
		super.addDictData("{DICT." + TYPE + "." + NOTICE + "}", NOTICE, 1);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
