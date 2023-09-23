package com.chestnut.contentcore.util;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsPageWidget;

public class PageWidgetUtils {

	public static String getStaticFileName(CmsPageWidget pw, String staticSuffix) {
		return pw.getCode() + StringUtils.DOT + staticSuffix;
	}
}
