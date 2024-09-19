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

import org.apache.commons.lang3.RandomUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HttpUtils {

	public final static String[] USER_AGENTS = new String[] {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50" };

	public static String randomUserAgent() {
		int index = RandomUtils.nextInt(0, USER_AGENTS.length);
		return USER_AGENTS[index];
	}

	private static SSLContext trustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				}
		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new SecureRandom());
		return sslContext;
	}

	private static HttpClient buildHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
		return buildHttpClient(false);
	}

	private static HttpClient buildHttpClient(boolean ignoreSSL) throws NoSuchAlgorithmException, KeyManagementException {
		return buildHttpClient(ignoreSSL, Duration.ofSeconds(30));
	}

	private static HttpClient buildHttpClient(boolean ignoreSSL, Duration connectTimeout) throws NoSuchAlgorithmException, KeyManagementException {
		HttpClient.Builder builder = HttpClient.newBuilder()
				.connectTimeout(Objects.requireNonNullElse(connectTimeout, Duration.ofSeconds(30)))
				.followRedirects(Redirect.ALWAYS);
		if (ignoreSSL) {
			builder.sslContext(trustAllSSLContext());
		}
		return builder.build();
	}

	/**
	 * 发起一个简单的GET请求，同步返回字符串结果
	 * <p>
	 * Timeout: 30s
	 * </p>
	 *
	 * @param uri
	 * @return
	 */
	public static String get(URI uri) {
		try {
			HttpClient httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.followRedirects(Redirect.ALWAYS)
					.build();
			HttpRequest httpRequest = HttpRequest.newBuilder(uri)
					.header("User-Agent", USER_AGENTS[0])
					.GET()
					.build();
			return httpClient.send(httpRequest, BodyHandlers.ofString()).body();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static String post(URI uri, String body, Map<String, String> headers) throws Exception {
		if (Objects.isNull(body)) {
			body = StringUtils.EMPTY;
		}
		HttpClient httpClient = buildHttpClient(true);
		HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
				.POST(BodyPublishers.ofString(body, StandardCharsets.UTF_8));
		headers.forEach(builder::header);
		if (!headers.containsKey("Content-Type")) {
			builder.header("Content-Type", "application/json");
		}
		HttpRequest httpRequest = builder.build();
		return httpClient.send(httpRequest, BodyHandlers.ofString()).body();
	}

	/**
	 * 发起一个简单的POST请求，同步返回字符串结果
	 * <p>
	 * Timeout: 30s
	 * Content-Type: application/json
	 * </p>
	 *
	 * @param uri
	 * @param jsonBody
	 * @return
	 */
	public static String postJSON(URI uri, String jsonBody) throws Exception {
		return post(uri, jsonBody, Map.of("User-Agent", USER_AGENTS[0]));
	}


	public static byte[] syncDownload(String uri) throws Exception {
		return syncDownload(uri, false);
	}

	public static byte[] syncDownload(String uri, boolean ignoreSSL) throws Exception {
		HttpClient httpClient = buildHttpClient(ignoreSSL);
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		return httpClient.send(httpRequest, BodyHandlers.ofByteArray()).body();
    }

	public static InputStream syncDownloadInputStream(String uri) throws Exception {
		return syncDownloadInputStream(uri, true);
	}

	public static InputStream syncDownloadInputStream(String uri, boolean ignoreSSL) throws Exception {
		HttpClient httpClient = buildHttpClient(ignoreSSL);
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		return httpClient.send(httpRequest, BodyHandlers.ofInputStream()).body();
	}

	/**
	 * 下载文件到指定路径
	 *
	 * @param uri
	 * @param destPath
	 */
	public static void asyncDownload(String uri, Path destPath) throws Exception {
		HttpClient httpClient = buildHttpClient(true);
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		httpClient.sendAsync(httpRequest, BodyHandlers.ofFile(destPath));
	}

	public static void asyncDownload(String uri, String destPath) throws Exception {
		asyncDownload(uri, Path.of(destPath));
	}

	/**
	 * 通过丢弃响应体的get请求获取响应头信息，抛弃get响应body
	 *
	 * @param uri
	 */
	public static String getDiscardingContentType(String uri) throws Exception {
		return getDiscardingHeader(uri, "content-type");
	}

	/**
	 * 通过丢弃响应体的get请求获取响应头信息，抛弃get响应body
	 *
	 * @param uri
	 * @param headerName
	 */
	public static String getDiscardingHeader(String uri, String headerName) throws Exception {
		HttpClient httpClient = buildHttpClient(true);
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		Optional<String> headerValue = httpClient.send(httpRequest, BodyHandlers.discarding()).headers().firstValue(headerName);
        return headerValue.orElse(null);
    }
}
