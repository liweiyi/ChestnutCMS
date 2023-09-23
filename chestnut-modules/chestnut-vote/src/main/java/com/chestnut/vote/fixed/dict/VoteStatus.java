package com.chestnut.vote.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 问卷调查状态
 */
@Component(FixedDictType.BEAN_PREFIX + VoteStatus.TYPE)
public class VoteStatus extends FixedDictType {

	public static final String TYPE = "VoteStatus";

	public static final String NORMAL = "0"; // 正常

	public static final String STOP = "1"; // 停用

	public static final String CLOSE = "2"; // 关闭，关闭的问卷调查表示归档不可再变更
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + NORMAL + "}", NORMAL, 1);
		super.addDictData("{DICT." + TYPE + "." + STOP + "}", STOP, 2);
		super.addDictData("{DICT." + TYPE + "." + CLOSE + "}", CLOSE, 3);
	}
	
	public static boolean isClosed(String status) {
		return CLOSE.equals(status);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
