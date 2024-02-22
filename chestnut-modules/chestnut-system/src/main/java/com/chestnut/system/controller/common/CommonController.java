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
package com.chestnut.system.controller.common;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.fixed.config.SysUploadSizeLimit;
import com.chestnut.system.fixed.config.SysUploadTypeLimit;
import com.chestnut.system.security.AdminUserType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * 通用请求处理
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/common")
public class CommonController extends BaseRestController {

	private static final Logger log = LoggerFactory.getLogger(CommonController.class);

	/**
	 * 通用上传请求（单个）
	 */
	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/upload")
	public R<?> uploadFile(MultipartFile file) throws Exception {
		try {
			if (Objects.isNull(file) || file.isEmpty()) {
				return R.fail("Upload multipart file is empty.");
			}
			String ext = FileExUtils.getExtension(file.getOriginalFilename());
			// 校验上传文件大小及类型
			SysUploadSizeLimit.check(file.getSize());
			SysUploadTypeLimit.check(ext);
			// 默认按日期目录存储
			String path = DateUtils.datePath() + StringUtils.SLASH + IdUtils.simpleUUID() + StringUtils.DOT + ext;
			// 写入文件
			FileUtils.writeByteArrayToFile(new File(SystemConfig.getUploadDir() + path), file.getBytes());
			// 文件预览路径
			String src = SystemConfig.getResourcePrefix() + path;
			// 返回预览链接地址
			return R.ok(Map.of("fileName", src));
		} catch (Exception e) {
			return R.fail(e.getMessage());
		}
	}

	/**
	 * 本地资源通用下载
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/download")
	public void resourceDownload(String path, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			String downloadName = StringUtils.substringAfterLast(path, "/");
			
			StringBuilder contentDispositionValue = new StringBuilder();
			contentDispositionValue.append("attachment; filename=").append(downloadName).append(";")
					.append("filename*=").append("utf-8''").append(downloadName);

			response.setHeader("Content-disposition", contentDispositionValue.toString());
			response.setHeader("download-filename", downloadName);
			
			Files.copy(Path.of(SystemConfig.getUploadDir() + path), response.getOutputStream());
		} catch (Exception e) {
			log.error("下载文件失败", e);
		}
	}
}
