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
package com.chestnut.system.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.system")
public class SysProperties {

	/**
	 * 资源文件上传根目录
	 */
	private String uploadPath;
	
	/** 演示模式开关 */
	private boolean demoMode;

	/**
	 * 是否记录定时任务日志到数据库
	 */
	private boolean scheduleLog;

	/**
	 * 可上传文件类型
	 */
	private FileTypes upload;

	@Getter
	@Setter
	public static class FileTypes {
		private List<String> image = List.of("jpg", "jpeg", "png", "gif", "webp");
		private List<String> video = List.of("mp4", "mpg", "mpeg", "rmvb", "rm", "avi", "wmv", "mov", "flv");
		private List<String> audio = List.of("mp3", "wav", "wma", "ogg", "aiff", "aac", "flac", "mid");
		private List<String> file = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp",
				"psd", "ai", "tif", "tiff", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "fla", "swf", "js", "css",
				"shtml", "html", "htm", "txt", "ttf", "eot", "mp4", "avi", "rmvb", "mpg", "flv", "mpeg", "rm", "mov", "wmv",
				"wmp", "mp3", "wma", "wav", "ogg", "rar", "zip", "gz", "bz2", "z", "iso", "cab", "jar");
	}
}
