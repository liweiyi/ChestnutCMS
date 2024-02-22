/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.common.utils;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 客户端工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
public class ServletUtils {

	/**
	 * www主域
	 */
	public static final String WWW = "www.";

	/**
	 * http请求
	 */
	public static final String HTTP = "http://";

	/**
	 * https请求
	 */
	public static final String HTTPS = "https://";

	/**
	 * Cookie.path默认值
	 */
	private static final String COOKIE_PATH = "/";

	/**
	 * RequestHeader[User-Agent]
	 */
	public static final String HEADER_USER_AGENT = "User-Agent";

	/**
	 * RequestHeader[Referer]
	 */
	public static final String HEADER_REFERER = "Referer";

	/**
	 * RequestHeader[Accept-Language]
	 */
	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	public static final String UNKNOWN = "unknown";

	public static boolean isHttpUrl(String url) {
		return StringUtils.startsWithIgnoreCase(url, HTTP) || StringUtils.startsWithIgnoreCase(url, HTTPS);
	}

	public static String getAcceptLanaguage(HttpServletRequest request) {
		return getHeader(request, HEADER_ACCEPT_LANGUAGE);
	}

	public static String getUserAgent(HttpServletRequest request) {
		return getHeader(request, HEADER_USER_AGENT);
	}

	public static String getUserAgent() {
		return getHeader(getRequest(), HEADER_USER_AGENT);
	}

	public static UserAgent parseUserAgent(HttpServletRequest request) {
		return UserAgent.parseUserAgentString(getUserAgent(request));
	}

	public static String getReferer(HttpServletRequest request) {
		return getHeader(request, HEADER_REFERER);
	}

	/**
	 * 读取指定name的Header值
	 *
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getHeader(HttpServletRequest request, String name) {
		String header = request.getHeader(name);
		if (Objects.isNull(header)) {
			header = StringUtils.EMPTY;
		}
		return header;
	}

	/**
	 * 读取所有Header值
	 *
	 * @param request
	 * @return
	 */
	public static Map<String, String> getHeaderMap(HttpServletRequest request) {
		final Map<String, String> headerMap = new HashMap<>();

		final Enumeration<String> names = request.getHeaderNames();
		String name;
		while (names.hasMoreElements()) {
			name = names.nextElement();
			headerMap.put(name, request.getHeader(name));
		}
		return headerMap;
	}

	public static CaseInsensitiveMap<String, String> getHeaderCaseInsensitiveMap(HttpServletRequest request) {
		final CaseInsensitiveMap<String, String> headerMap = new CaseInsensitiveMap<>();
		final Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			headerMap.put(name, request.getHeader(name));
		}
		return headerMap;
	}

	/**
	 * response写入cookie
	 *
	 * @param response
	 * @param key
	 * @param value
	 */
	public static void setCookie(HttpServletResponse response, String key, String value) {
		setCookie(response, key, value, null, COOKIE_PATH, Integer.MAX_VALUE, true);
	}

	/**
	 * response写入cookie
	 *
	 * @param response
	 * @param key
	 * @param value
	 * @param maxAge
	 */
	public static void setCookie(HttpServletResponse response, String key, String value, String domain, String path,
								  int maxAge, boolean isHttpOnly) {
		Cookie cookie = new Cookie(key, value);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(isHttpOnly);
		response.addCookie(cookie);
	}

	/**
	 * 查询value
	 *
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest request, String key) {
		Cookie cookie = getCookie(request, key);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}

	public static Map<String, String> getCookieValues(HttpServletRequest request) {
		Cookie[] arr_cookie = request.getCookies();
		if (arr_cookie == null) {
			return Map.of();
		}
		return Stream.of(arr_cookie)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(c -> c.getName(), c -> c.getValue()));
	}

	/**
	 * 查询Cookie
	 *
	 * @param request
	 * @param key
	 */
	private static Cookie getCookie(HttpServletRequest request, String key) {
		Cookie[] arr_cookie = request.getCookies();
		if (arr_cookie != null && arr_cookie.length > 0) {
			for (Cookie cookie : arr_cookie) {
				if (cookie.getName().equals(key)) {
					return cookie;
				}
			}
		}
		return null;
	}


	/**
	 * 删除Cookie
	 *
	 * @param request
	 * @param response
	 * @param key
	 */
	public static void remove(HttpServletRequest request, HttpServletResponse response, String key) {
		Cookie cookie = getCookie(request, key);
		if (cookie != null) {
			setCookie(response, key, "", null, COOKIE_PATH, 0, true);
		}
	}

	public static Map<String, String> getParameters() {
		return getParameters(getRequest());
	}

	public static Map<String, String> getParameters(HttpServletRequest request) {
		Map<String, String> params = new HashMap<>();
		request.getParameterMap().forEach((k, y) -> {
			if (y.length > 0) {
				params.put(k, StringUtils.join(y));
			}
		});
		return params;
	}

	/**
	 * 获取String参数
	 */
	public static String getParameter(String name) {
		return getParameter(name, StringUtils.EMPTY);
	}

	/**
	 * 获取String参数
	 */
	public static String getParameter(String name, String defaultValue) {
		return getParameter(getRequest(), name, defaultValue);
	}

	/**
	 * 获取String参数
	 */
	public static String getParameter(ServletRequest request, String name, String defaultValue) {
		return ConvertUtils.toStr(request.getParameter(name), defaultValue);
	}

	/**
	 * 获取Integer参数
	 */
	public static int getParameterToInt(String name) {
		return getParameterToInt(name, 0);
	}

	/**
	 * 获取Integer参数
	 */
	public static int getParameterToInt(String name, int defaultValue) {
		return getParameterToInt(getRequest(), name, defaultValue);
	}

	/**
	 * 获取Integer参数
	 */
	public static int getParameterToInt(ServletRequest request, String name, int defaultValue) {
		return ConvertUtils.toInteger(request.getParameter(name), defaultValue);
	}

	/**
	 * 获取Boolean参数，默认：false
	 */
	public static boolean getParameterToBool(String name) {
		return getParameterToBool(getRequest(), name, false);
	}

	/**
	 * 获取Boolean参数
	 */
	public static boolean getParameterToBool(String name, boolean defaultValue) {
		return getParameterToBool(getRequest(), name, defaultValue);
	}

	/**
	 * 获取Boolean参数
	 */
	public static boolean getParameterToBool(ServletRequest request, String name, boolean defaultValue) {
		return ConvertUtils.toBoolean(request.getParameter(name), defaultValue);
	}

	/**
	 * 获得所有请求参数
	 *
	 * @param request 请求对象{@link ServletRequest}
	 * @return Map
	 */
	public static Map<String, String[]> getParams(ServletRequest request) {
		final Map<String, String[]> map = request.getParameterMap();
		return Collections.unmodifiableMap(map);
	}

	/**
	 * 获得所有请求参数
	 *
	 * @param request 请求对象{@link ServletRequest}
	 * @return Map
	 */
	public static Map<String, String> getParamMap(ServletRequest request) {
		Map<String, String> params = new HashMap<>();
		for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
			params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
		}
		return params;
	}

	/**
	 * 获取request
	 */
	public static HttpServletRequest getRequest() {
		return getRequestAttributes().getRequest();
	}

	/**
	 * 获取response
	 */
	public static HttpServletResponse getResponse() {
		return getRequestAttributes().getResponse();
	}

	/**
	 * 获取session
	 */
	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	public static ServletRequestAttributes getRequestAttributes() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		return (ServletRequestAttributes) attributes;
	}

	/**
	 * 将字符串渲染到客户端
	 *
	 * @param response 渲染对象
	 * @param string   待渲染的字符串
	 */
	public static void renderString(HttpServletResponse response, String string) {
		try {
			response.setStatus(200);
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getWriter().print(string);
		} catch (IOException e) {
			log.error("Render string `{}` to response fail.", string, e);
		}
	}

	public static String getRequestBodyAsString(ServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try (InputStream inputStream = request.getInputStream()) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				String line = "";
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}
		} catch (IOException e) {
			log.error("Read request body fail.", e);
		}
		return sb.toString();
	}

	public static byte[] getRequestBodyAsBytes(ServletRequest request) {
		int len = request.getContentLength();
		byte[] buffer = new byte[len];
		try (ServletInputStream in = request.getInputStream()) {
			in.read(buffer, 0, len);
		} catch (IOException e) {
			log.error("Read request body to bytes fail.", e);
		}
		return buffer;
	}

	/**
	 * 是否是Ajax异步请求
	 *
	 * @param request
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String accept = request.getHeader("accept");
		if (accept != null && accept.contains("application/json")) {
			return true;
		}

		String xRequestedWith = request.getHeader("X-Requested-With");
		if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
			return true;
		}

		String uri = request.getRequestURI();
		if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml")) {
			return true;
		}

		String ajax = request.getParameter("__ajax");
		return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
	}

	/**
	 * 内容编码
	 *
	 * @param str 内容
	 * @return 编码后的内容
	 */
	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * 内容解码
	 *
	 * @param str 内容
	 * @return 解码后的内容
	 */
	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * 获取客户端IP
	 *
	 * @param request 请求对象
	 * @return IP地址
	 */
	public static String getIpAddr(HttpServletRequest request) {
		if (request == null) {
			return UNKNOWN;
		}
		String ip = request.getHeader("x-forwarded-for");
		if (isUnknown(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (isUnknown(ip)) {
			ip = request.getHeader("X-Forwarded-For");
		}
		if (isUnknown(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (isUnknown(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (isUnknown(ip)) {
			ip = request.getRemoteAddr();
		}
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
	}

	/**
	 * 获取IP地址
	 *
	 * @return 本地IP地址
	 */
	public static String getHostIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		return "127.0.0.1";
	}

	/**
	 * 获取主机名
	 *
	 * @return 本地主机名
	 */
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		return UNKNOWN;
	}

	/**
	 * 从多级反向代理中获得第一个非unknown IP地址
	 *
	 * @param ip 获得的IP地址
	 * @return 第一个非unknown IP地址
	 */
	public static String getMultistageReverseProxyIp(String ip) {
		// 多级反向代理检测
		if (ip != null && ip.indexOf(",") > 0) {
			final String[] ips = ip.trim().split(",");
			for (String subIp : ips) {
				if (false == isUnknown(subIp)) {
					ip = subIp;
					break;
				}
			}
		}
		return ip;
	}

	/**
	 * 检测给定字符串是否为未知，多用于检测HTTP请求相关
	 *
	 * @param checkString 被检测的字符串
	 * @return 是否未知
	 */
	public static boolean isUnknown(String checkString) {
		return StringUtils.isBlank(checkString) || UNKNOWN.equalsIgnoreCase(checkString);
	}

	/**
	 * 检查是否为内部IP地址
	 *
	 * @param ip IP地址
	 * @return 结果
	 */
	public static boolean internalIp(String ip) {
		byte[] addr = textToNumericFormatV4(ip);
		return internalIp(addr) || "127.0.0.1".equals(ip);
	}

	/**
	 * 检查是否为内部IP地址
	 *
	 * @param addr byte地址
	 * @return 结果
	 */
	private static boolean internalIp(byte[] addr) {
		if (Objects.isNull(addr) || addr.length < 2) {
			return true;
		}
		final byte b0 = addr[0];
		final byte b1 = addr[1];
		// 10.x.x.x/8
		final byte SECTION_1 = 0x0A;
		// 172.16.x.x/12
		final byte SECTION_2 = (byte) 0xAC;
		final byte SECTION_3 = (byte) 0x10;
		final byte SECTION_4 = (byte) 0x1F;
		// 192.168.x.x/16
		final byte SECTION_5 = (byte) 0xC0;
		final byte SECTION_6 = (byte) 0xA8;
		switch (b0) {
			case SECTION_1:
				return true;
			case SECTION_2:
				if (b1 >= SECTION_3 && b1 <= SECTION_4) {
					return true;
				}
			case SECTION_5:
				switch (b1) {
					case SECTION_6:
						return true;
				}
			default:
				return false;
		}
	}

	/**
	 * 将IPv4地址转换成字节
	 *
	 * @param text IPv4地址
	 * @return byte 字节
	 */
	public static byte[] textToNumericFormatV4(String text) {
		if (text.length() == 0) {
			return null;
		}

		byte[] bytes = new byte[4];
		String[] elements = text.split("\\.", -1);
		try {
			long l;
			int i;
			switch (elements.length) {
				case 1:
					l = Long.parseLong(elements[0]);
					if ((l < 0L) || (l > 4294967295L)) {
						return null;
					}
					bytes[0] = (byte) (int) (l >> 24 & 0xFF);
					bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
					bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
					bytes[3] = (byte) (int) (l & 0xFF);
					break;
				case 2:
					l = Integer.parseInt(elements[0]);
					if ((l < 0L) || (l > 255L)) {
						return null;
					}
					bytes[0] = (byte) (int) (l & 0xFF);
					l = Integer.parseInt(elements[1]);
					if ((l < 0L) || (l > 16777215L)) {
						return null;
					}
					bytes[1] = (byte) (int) (l >> 16 & 0xFF);
					bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
					bytes[3] = (byte) (int) (l & 0xFF);
					break;
				case 3:
					for (i = 0; i < 2; ++i) {
						l = Integer.parseInt(elements[i]);
						if ((l < 0L) || (l > 255L)) {
							return null;
						}
						bytes[i] = (byte) (int) (l & 0xFF);
					}
					l = Integer.parseInt(elements[2]);
					if ((l < 0L) || (l > 65535L)) {
						return null;
					}
					bytes[2] = (byte) (int) (l >> 8 & 0xFF);
					bytes[3] = (byte) (int) (l & 0xFF);
					break;
				case 4:
					for (i = 0; i < 4; ++i) {
						l = Integer.parseInt(elements[i]);
						if ((l < 0L) || (l > 255L)) {
							return null;
						}
						bytes[i] = (byte) (int) (l & 0xFF);
					}
					break;
				default:
					return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return bytes;
	}

	/**
	 * 根据User-Agent解析客户端类型
	 *
	 * @return
	 */
	public static String getDeviceType() {
		String userAgent = ServletUtils.getUserAgent();
		return getDeviceType(userAgent);
	}

	/**
	 * 根据User-Agent解析客户端类型
	 *
	 * @param userAgent
	 * @return
	 */
	public static String getDeviceType(String userAgent) {
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		return ua.getOperatingSystem().getDeviceType().getName();
	}
}
