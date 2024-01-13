package com.chestnut.common.staticize.func.impl;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Freemarker模板自定义函数：处理内部链接
 */
@Component
@RequiredArgsConstructor
public class URLParametersFunction extends AbstractFunc  {

	static final String FUNC_NAME = "urlParameters";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 1) {
			return StringUtils.EMPTY;
		}
		String url = args[0].toString();
		String queryString = StringUtils.substringAfter(url, "?");
		List<Map<String, String>> list = new ArrayList<>();
		StringUtils.splitToMap(queryString, "&", "=").forEach((k, v) -> {
			list.add(Map.of(
					"name", k,
					"value", v
			));
		});
		return list;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of();
	}
}
