package com.chestnut.search.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

/**
 * 分词库自定义词类型
 */
@Component(FixedDictType.BEAN_PREFIX + WordAnalyzeType.TYPE)
public class WordAnalyzeType extends FixedDictType {

	public static final String TYPE = "WordAnalyzeType";

	public static final String IKSmart = "ik_smart";

	public static final String IKMaxWord = "ik_max_word";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public WordAnalyzeType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + IKSmart + "}", IKSmart, 1);
		super.addDictData("{DICT." + TYPE + "." + IKMaxWord + "}", IKMaxWord, 2);
	}
}
