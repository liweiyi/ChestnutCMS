package com.chestnut.contentcore.template.func;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.StringUtils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：清理html标签
 */
@Component
@RequiredArgsConstructor
public class ClearHtmlTagFunction extends AbstractFunc  {

	private static final String FUNC_NAME = "clearHtmlTag";
	
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
		if (args.length != 1 || Objects.isNull(args[0])) {
			return StringUtils.EMPTY;
		}
		SimpleScalar simpleScalar = (SimpleScalar) args[0];
		return HtmlUtils.clean(simpleScalar.getAsString());
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("待处理字符串", FuncArgType.String, true, null));
	}
}
