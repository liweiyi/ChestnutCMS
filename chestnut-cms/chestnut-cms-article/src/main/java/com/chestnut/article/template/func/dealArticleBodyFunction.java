package com.chestnut.article.template.func;

import com.chestnut.article.ArticleUtils;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：处理Html文本内容中的内部链接
 */
@Component
@RequiredArgsConstructor
public class dealArticleBodyFunction extends AbstractFunc  {

	static final String FUNC_NAME = "dealArticleBody";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private final IArticleService articleService;

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
		SimpleScalar simpleScalar = (SimpleScalar) args[0];
		String contentHtml = simpleScalar.getAsString();
		// 处理内容扩展模板占位符
		contentHtml = ArticleUtils.dealContentEx(contentHtml, context.getPublishPipeCode(), context.isPreview());
		// 处理正文内部链接
		contentHtml = InternalUrlUtils.dealInternalUrl(contentHtml, context.getPublishPipeCode(), context.isPreview());
		return contentHtml;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("HTML文章正文内容", FuncArgType.String, true, null));
	}
}
