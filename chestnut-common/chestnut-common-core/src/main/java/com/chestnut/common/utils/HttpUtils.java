package com.chestnut.common.utils;

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
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.RandomUtils;

public class HttpUtils {

	public final static String[] USER_AGENTS = new String[] {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50" };

	public static String randomUserAgent() {
		int index = RandomUtils.nextInt(0, USER_AGENTS.length);
		return USER_AGENTS[index];
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

	/**
	 * 发起一个简单的POST请求，同步返回字符串结果
	 * <p>
	 * Timeout: 30s
	 * Content-Type: application/json
	 * </p>
	 *
	 * @param uri
	 * @param body
	 * @return
	 */
	public static String postJSON(URI uri, String body) {
		try {
			if (Objects.isNull(body)) {
				body = StringUtils.EMPTY;
			}
			HttpClient httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.followRedirects(Redirect.ALWAYS)
					.build();
			HttpRequest httpRequest = HttpRequest.newBuilder(uri)
					.header("Content-Type", "application/json")
					.header("User-Agent", USER_AGENTS[0])
					.POST(BodyPublishers.ofString(body, StandardCharsets.UTF_8))
					.build();
			return httpClient.send(httpRequest, BodyHandlers.ofString()).body();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] syncDownload(String uri) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.followRedirects(Redirect.ALWAYS)
				.build();
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		try {
			return httpClient.send(httpRequest, BodyHandlers.ofByteArray()).body();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static InputStream syncDownloadInputStream(String uri) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.followRedirects(Redirect.ALWAYS)
				.build();
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		try {
			return httpClient.send(httpRequest, BodyHandlers.ofInputStream()).body();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		syncDownload("http://www.liandu24.com/wp-content/uploads/2023/07/2023070513161507784652.png");
	}

	/**
	 * 通过丢弃响应体的get请求获取响应头信息，抛弃get响应body
	 *
	 * @param uri
	 * @param headerName
	 * @return
	 */
	public static String getDiscardingContentType(String uri) {
		return getDiscardingHeader(uri, "content-type");
	}

	/**
	 * 通过丢弃响应体的get请求获取响应头信息，抛弃get响应body
	 *
	 * @param uri
	 * @param headerName
	 * @return
	 */
	public static String getDiscardingHeader(String uri, String headerName) {
		try {
			HttpClient httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.followRedirects(Redirect.ALWAYS)
					.build();
			HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
					.setHeader("User-Agent", randomUserAgent())
					.GET().build();
			Optional<String> headerValue = httpClient.send(httpRequest, BodyHandlers.discarding()).headers().firstValue(headerName);
			if (headerValue.isPresent()) {
				return headerValue.get();
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * 下载文件到指定路径
	 *
	 * @param uri
	 * @param destPath
	 */
	public static void asyncDownload(String uri, Path destPath) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.followRedirects(Redirect.ALWAYS)
				.build();
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(uri))
				.setHeader("User-Agent", randomUserAgent())
				.GET().build();
		httpClient.sendAsync(httpRequest, BodyHandlers.ofFile(destPath));
	}

	public static void asyncDownload(String uri, String destPath) {
		asyncDownload(uri, Path.of(destPath));
	}
}
