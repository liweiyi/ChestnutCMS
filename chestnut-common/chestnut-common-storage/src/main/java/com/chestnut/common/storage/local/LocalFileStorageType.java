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
package com.chestnut.common.storage.local;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.storage.*;
import com.chestnut.common.storage.exception.FileStorageException;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component(IFileStorageType.BEAN_NAME_PREIFX + LocalFileStorageType.TYPE)
public class LocalFileStorageType implements IFileStorageType {

	public final static String TYPE = "Local";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getName() {
		return I18nUtils.get("{STORAGE.TYPE." + TYPE + "}");
	}

	@Override
	public boolean exists(StorageExistArgs args) {
		String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
		bucket = StringUtils.appendIfMissing(bucket, "/");
		String filePath = bucket + args.getPath();
		File file = new File(filePath);
		return file.exists() && file.isFile();
	}

	@Override
	public void copy(StorageCopyArgs args) {
		try {
			String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
			bucket = StringUtils.appendIfMissing(bucket, "/");
			FileUtils.copyDirectory(new File(bucket + args.getSourcePath()), new File(bucket + args.getDestPath()));
		} catch (IOException e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void move(StorageMoveArgs args) {
		try {
			String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
			bucket = StringUtils.appendIfMissing(bucket, "/");
			FileUtils.moveDirectory(new File(bucket + args.getSourcePath()), new File(bucket + args.getDestPath()));
		} catch (IOException e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public InputStream read(StorageReadArgs args) {
		try {
			String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
			bucket = StringUtils.appendIfMissing(bucket, "/");
			String filePath = bucket + args.getPath();
			File file = new File(filePath);
			if (!file.exists()) {
				throw new FileNotFoundException();
			}
			return new FileInputStream(filePath);
		} catch (IOException e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void write(StorageWriteArgs args) {
		try {
			String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
			bucket = StringUtils.appendIfMissing(bucket, "/");
			String filePath = bucket + args.getPath();
			File file = new File(filePath);
			FileExUtils.mkdirs(file.getParentFile().getAbsolutePath());
			FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(args.getInputStream()));
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		try {
			String bucket = Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY);
			bucket = StringUtils.appendIfMissing(bucket, "/");
			String filePath = bucket + args.getPath();
			FileUtils.delete(new File(filePath));
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public List<String> list(StorageListArgs args) {
		try {
			String bucket = StringUtils.appendIfMissing(
					Objects.requireNonNullElse(args.getBucket(), StringUtils.EMPTY), "/");
			String dirPath = bucket;
			String prefix;
			if (args.getPrefix().indexOf("/") > 0) {
				dirPath += StringUtils.substringBeforeLast(args.getPrefix(), "/");
				prefix = StringUtils.substringAfterLast(args.getPrefix(), "/");
			} else {
				prefix = args.getPrefix();
			}
			File directory = new File(dirPath);
			if (!directory.isDirectory()) {
				return List.of();
			}
			if (StringUtils.isNotEmpty(prefix)) {
				File[] files = directory.listFiles(f -> f.getName().startsWith(prefix));
				if (Objects.nonNull(files)) {
					return Stream.of(files).map(f -> {
						return FileExUtils.normalizePath(f.getAbsolutePath()).substring(bucket.length());
					}).toList();
				}
			} else {
				File[] files = directory.listFiles();
				if (Objects.nonNull(files)) {
					return Stream.of(files).map(f -> {
						return FileExUtils.normalizePath(f.getAbsolutePath()).substring(bucket.length());
					}).toList();
				}
			}
			return List.of();
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}
}
