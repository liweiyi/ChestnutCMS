package com.chestnut.seo.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Sitemap页面类型
 */
@Component(FixedDictType.BEAN_PREFIX + SitemapPageType.TYPE)
public class SitemapPageType extends FixedDictType {

	public static final String TYPE = "CMSSitemapPageType";

	public static final String PC = "pc";

	public static final String Mobile = "mobile";

	public static final String PC_Mobile = "pc,mobile";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public SitemapPageType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + PC + "}", PC, 1);
		super.addDictData("{DICT." + TYPE + "." + Mobile + "}", Mobile, 2);
		super.addDictData("{DICT." + TYPE + ".pc_mobile}", PC_Mobile, 3);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
