package com.chestnut.search.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 分词库自定义词类型
 */
@Component(FixedDictType.BEAN_PREFIX + SearchDictWordType.TYPE)
public class SearchDictWordType extends FixedDictType {

	public static final String TYPE = "SearchDictWordType";

	public static final String WORD = "WORD";

	public static final String STOP = "STOP";
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public SearchDictWordType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + WORD + "}", WORD, 1);
		super.addDictData("{DICT." + TYPE + "." + STOP + "}", STOP, 2);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
