/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG2_DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Desc}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";

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
				new FuncArg(ARG1_NAME, FuncArgType.String, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, false, null, "100%"),
				new FuncArg(ARG3_NAME, FuncArgType.String, false)
		);
	}
}
