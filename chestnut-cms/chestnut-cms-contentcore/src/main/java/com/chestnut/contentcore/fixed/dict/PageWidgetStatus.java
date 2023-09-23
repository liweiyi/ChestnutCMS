package com.chestnut.contentcore.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 页面部件状态
 */
@Component(FixedDictType.BEAN_PREFIX + PageWidgetStatus.TYPE)
public class PageWidgetStatus extends FixedDictType {

	public static final String TYPE = "CMSPageWidgetStatus";

	public static final String DRAFT = "0"; // 初稿

	public static final String PUBLISHED = "30"; // 已发布

	public static final String OFFLINE = "40"; // 已下线

	public static final String EDITING = "60"; // 重新编辑

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public PageWidgetStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + DRAFT + "}", DRAFT, 1);
		super.addDictData("{DICT." + TYPE + "." + PUBLISHED + "}", PUBLISHED, 2);
		super.addDictData("{DICT." + TYPE + "." + OFFLINE + "}", OFFLINE, 3);
		super.addDictData("{DICT." + TYPE + "." + EDITING + "}", EDITING, 4);
	}

	public static boolean isPublished(String v) {
		return PUBLISHED.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

}
