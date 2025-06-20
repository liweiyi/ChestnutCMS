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
package com.chestnut.contentcore.util;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.exception.InternalUrlParseException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内部链接处理工具类
 */
@Slf4j
public class InternalUrlUtils {

    /**
     * 带iurl属性值的任意标签匹配
     */
	public static final Pattern InternalUrlTagPattern = Pattern.compile("<[^>]+=\\s*['\"](iurl://[^'\"]+)['\"][^>]*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);


	public static String getInternalUrl(String type, Long dataId) {
		return new InternalURL(type, dataId).toIUrl();
	}
	
	public static String getInternalUrl(String type, Long dataId, String path) {
		return new InternalURL(type, dataId, path).toIUrl();
	}
	
	/**
	 * 内部URL，自定义格式，方便各处使用
	 *
	 * @param type
	 * @param dataId
	 * @param path
	 * @return
	 */
	public static String getInternalUrl(String type, Long dataId, String path, Map<String, String> params) {
		return new InternalURL(type, dataId, path, params).toIUrl();
	}

	/**
	 * 根据IURL返回真实链接
	 * 
	 * @param iurl
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	public static String getActualUrl(String iurl, String publishPipeCode, boolean isPreview) {
		if (isInternalUrl(iurl)) {
			InternalURL internalUrl = parseInternalUrl(iurl);
			return getActualUrl(internalUrl, publishPipeCode, isPreview);
		}
		return iurl;
	}

	public static String getActualUrl(InternalURL internalUrl, String publishPipeCode, boolean isPreview) {
		try {
			IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(internalUrl.getType());
			return internalDataType.getLink(internalUrl, 1, publishPipeCode, isPreview);
		} catch (Exception e) {
			throw new InternalUrlParseException(e.getMessage(), e);
		}
	}

	/**
	 * 获取资源预览链接, 仅适用于publishPipe无关的资源文件路径
	 * 
	 * @param iurl
	 * @return
	 */
	public static String getActualPreviewUrl(String iurl) {
		return getActualUrl(iurl, null, true);
	}

	public static String getActualPreviewUrl(InternalURL internalURL) {
		return getActualUrl(internalURL, null, true);
	}

	/**
	 * 判断是否是内部资源URL
	 */
	public static boolean isInternalUrl(String url) {
		return StringUtils.isNotEmpty(url) && url.toLowerCase().startsWith(InternalURL.IURLProtocol);
	}

	/**
	 * 解析内部资源URL
	 * 
	 * @param url
	 * @return
	 */
	public static InternalURL parseInternalUrl(String url) {
		if (!isInternalUrl(url)) {
			return null;
		}
		return InternalURL.parse(url);
	}

	/**
	 * 处理内部链接，替换src/href属性值
	 *
	 * @param contentHtml
	 * @return
	 */
	public static String dealResourceInternalUrl(String contentHtml) {
		return dealInternalUrl(contentHtml, null, true);
	}

	/**
	 * 处理HTML内容中的内部链接
	 * 
	 * 查找所有带iurl链接的标签，将链接替换成真实访问地址，预览模式需要添加iurl标签属性保存iurl地址
	 * 
	 * @param contentHtml
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	public static String dealInternalUrl(String contentHtml, String publishPipeCode, boolean isPreview) {
		if (StringUtils.isEmpty(contentHtml)) {
			return contentHtml;
		}
		Matcher matcher = InternalUrlTagPattern.matcher(contentHtml);
		StringBuilder html = new StringBuilder();
		int index = 0;
		while (matcher.find()) {
			String tagStr = matcher.group();
			String iurl = matcher.group(1);
			// begin
			try {
				InternalURL internalUrl = parseInternalUrl(iurl);
				if (Objects.nonNull(internalUrl)) {
					String actualUrl = getActualUrl(internalUrl, publishPipeCode, isPreview);
					tagStr = StringUtils.replaceEx(tagStr, iurl, actualUrl);
					if(isPreview) {
						String end = tagStr.endsWith("/>") ? "/>" : ">";
						// 预览模式添加iurl属性
						tagStr = StringUtils.substringBeforeLast(tagStr, end) + " iurl=\"" + iurl + "\"" + end;
					}
				}
			} catch (Exception e) {
				log.warn("InternalUrl parse failed: " + iurl, e);
			}
			// end
			html.append(contentHtml, index, matcher.start()).append(tagStr);
			index = matcher.end();
		}
		html.append(contentHtml.substring(index));
		return html.toString();
	}
}
