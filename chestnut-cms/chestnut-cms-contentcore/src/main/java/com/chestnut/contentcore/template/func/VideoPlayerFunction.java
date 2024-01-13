package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Freemarker模板自定义函数：处理Html文本内容中的视频资源链接，将资源链接替换成视频播放器
 */
@Component
@RequiredArgsConstructor
public class VideoPlayerFunction extends AbstractFunc  {

	static final String FUNC_NAME = "videoPlayer";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	public static final Pattern VideoATagPattern = Pattern.compile("<a\\s[^>]*class\\s*=\\s*['\"]art-body-video['\"][^>]*>.*?</a>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	public static final Pattern HrefPattern = Pattern.compile("\\s*href\\s*=\\s*['\"]([^'\"]+)['\"]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

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
		String width = "100%";
		String height = "";
		if (args.length > 1) {
			width = args[1].toString();
		}
		if (args.length > 2) {
			height = args[2].toString();
		}
		String contentHtml = args[0].toString();
		Matcher matcher = VideoATagPattern.matcher(contentHtml);
		StringBuilder html = new StringBuilder();
		int index = 0;
		while (matcher.find()) {
			String tagStr = matcher.group();
			Matcher srcMatcher = HrefPattern.matcher(tagStr);
			if (srcMatcher.find()) {
				String src = srcMatcher.group(1);
				tagStr = """
						<video controls width="%s" height="%s">
						  <source src="%s">
						  Sorry, your browser doesn't support embedded videos.
						</video>
						""".formatted(width, height, src);
			}
			html.append(contentHtml, index, matcher.start()).append(tagStr);
			index = matcher.end();
		}
		html.append(contentHtml.substring(index));
		return html.toString();
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("{FREEMARKER.FUNC."+FUNC_NAME+".Arg1.Name}", FuncArgType.String, true, null),
				new FuncArg("{FREEMARKER.FUNC."+FUNC_NAME+".Arg2.Name}", FuncArgType.String, false, "{FREEMARKER.FUNC."+FUNC_NAME+".Arg2.Desc}"),
				new FuncArg("{FREEMARKER.FUNC."+FUNC_NAME+".Arg3.Name}", FuncArgType.String, false, null)
		);
	}
}
