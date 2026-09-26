/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.system.controller.common;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.security.AdminUserType;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 通用请求处理
 */
@XComment("{API.DOC.COMMON.MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/common")
public class CommonController extends BaseRestController {

	private static final Logger log = LoggerFactory.getLogger(CommonController.class);

	/**
	 * 本地资源通用下载
	 */
	@XComment("{API.DOC.COMMON.DOWNLOAD_FILE}")
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/download")
	public void resourceDownload(String path, HttpServletResponse response) throws IOException {
		try {
			Path file = resolvePublicFile(path);
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			String downloadName = file.getFileName().toString();
			String contentDispositionValue = "attachment; filename=" + downloadName + ";" +
					"filename*=" + "utf-8''" + downloadName;
			response.setHeader("Content-disposition", contentDispositionValue);
			response.setHeader("download-filename", downloadName);
			Files.copy(file, response.getOutputStream());
		} catch (Exception e) {
			log.error("下载文件失败", e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	static Path resolvePublicFile(String path) throws IOException {
		Path root = Path.of(SystemConfig.getPublicFileUploadDir()).toRealPath();
		Path relative = Path.of(path);
		if (relative.isAbsolute()) {
			throw new SecurityException("Absolute download path is not allowed");
		}
		Path file = root.resolve(relative).normalize();
		if (!file.startsWith(root)) {
			throw new SecurityException("Download path must point to a public file");
		}
		Path realFile = file.toRealPath();
		if (!realFile.startsWith(root) || !Files.isRegularFile(realFile)) {
			throw new SecurityException("Download path must point to a public file");
		}
		return realFile;
	}
}
