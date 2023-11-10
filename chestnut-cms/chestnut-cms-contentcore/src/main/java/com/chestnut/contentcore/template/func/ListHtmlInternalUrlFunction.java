package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Freemarker模板自定义函数：处理Html文本内容中的内部链接
 */
@Component
@RequiredArgsConstructor
public class ListHtmlInternalUrlFunction extends AbstractFunc  {

	static final String FUNC_NAME = "listHtmlInternalUrl";

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
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());

		String html = args[0].toString();
		Matcher matcher = InternalUrlUtils.InternalUrlTagPattern.matcher(html);
		List<String> list = new ArrayList<>();
		while (matcher.find()) {
			String iurl = matcher.group(1);
			String actualUrl = InternalUrlUtils.getActualUrl(iurl, context.getPublishPipeCode(), context.isPreview());
			list.add(actualUrl);
		}
		return list;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("HTML文本内容", FuncArgType.String, true, null));
	}
}
