package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 通知状态：正常/关闭
 */
@Component(FixedDictType.BEAN_PREFIX + NoticeStatus.TYPE)
public class NoticeStatus extends FixedDictType {

	public static final String TYPE = "NoticeStatus";

	public static final String NORMAL = "0";

	public static final String CLOSED = "1";
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public NoticeStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + NORMAL + "}", NORMAL, 1);
		super.addDictData("{DICT." + TYPE + "." + CLOSED + "}", CLOSED, 2);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
