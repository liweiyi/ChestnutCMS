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
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.util.ContentCoreUtils;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Freemarker模板自定义函数：处理Html文本内容中的文件资源链接，返回链接列表
 */
@Component
@RequiredArgsConstructor
public class FileExtractorFunction extends AbstractFunc  {

	static final String FUNC_NAME = "fileExtractor";
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	public static final Pattern FileATagPattern = Pattern.compile("<a\\s[^>]*class\\s*=\\s*['\"]art-body-file['\"][^>]*>.*?</a>",
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
			return List.of();
		}
		List<String> suffixArray = List.of();
		if (args.length == 2 && Objects.nonNull(args[1])) {
			String arg2 = args[1].toString();
			if (arg2.startsWith(StringUtils.DOT)) {
				String[] arr = StringUtils.split(arg2, StringUtils.COMMA);
				suffixArray = Stream.of(arr).map(s -> s.substring(1)).toList();
			} else {
				IResourceType resourceType = ContentCoreUtils.getResourceType(arg2.toLowerCase());
				if (Objects.nonNull(resourceType)) {
					suffixArray = Arrays.asList(resourceType.getUsableSuffix());
				}
			}
		}
		String contentHtml = args[0].toString();
		Matcher matcher = FileATagPattern.matcher(contentHtml);
		ArrayList<String> fileLinks = new ArrayList<>();
		while (matcher.find()) {
			String tagStr = matcher.group();
			Matcher srcMatcher = HrefPattern.matcher(tagStr);
			if (srcMatcher.find()) {
				String src = srcMatcher.group(1);
				if (suffixArray.isEmpty() || suffixArray.contains(src)) {
					fileLinks.add(src);
				}
			}
		}
		return fileLinks;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true, null),
				new FuncArg(ARG2_NAME, FuncArgType.String, false, null)
		);
	}
}
