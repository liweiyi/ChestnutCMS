package com.chestnut.vote.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 问卷调查查看方式
 */
@Component(FixedDictType.BEAN_PREFIX + VoteViewType.TYPE)
public class VoteViewType extends FixedDictType {

	public static final String TYPE = "VoteViewType";

	public static final String INVISIBLE = "0"; // 不可查看

	public static final String AFTER_VOTE = "1"; // 提交后查看

	public static final String VISIBLE = "2"; // 不限制
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteViewType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + INVISIBLE + "}", INVISIBLE, 1);
		super.addDictData("{DICT." + TYPE + "." + AFTER_VOTE + "}", AFTER_VOTE, 2);
		super.addDictData("{DICT." + TYPE + "." + VISIBLE + "}", VISIBLE, 3);
	}
	
	public static boolean isInvisible(String type) {
		return INVISIBLE.equals(type);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
