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

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.storage.*;
import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.FileStorageArgsProperty;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import lombok.Getter;

import java.io.*;
import java.util.List;

/**
 * FileStorageHelper
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class FileStorageHelper {

    private IFileStorageType fileStorage;

    private String accessKey;

    private String accessSecret;

    private String bucket;

    private String endpoint;

    private String region;

    private String pipeline;

    @Getter
    private String domain;

    public static FileStorageHelper of(IFileStorageType fst, CmsSite site) {
        String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
        FileStorageArgsProperty.FileStorageArgs args = FileStorageArgsProperty.getValue(site.getConfigProps());
        if (LocalFileStorageType.TYPE.equals(fileStorageType)) {
            // 本地存储特殊处理
            args.setBucket(SiteUtils.getSiteResourceRoot(site));
        }
        FileStorageHelper helper = new FileStorageHelper();
        helper.fileStorage = fst;
        helper.accessKey = args.getAccessKey();
        helper.accessSecret = args.getAccessSecret();
        helper.bucket = args.getBucket();
        helper.endpoint = args.getEndpoint();
        helper.region = args.getRegion();
        helper.pipeline = args.getPipeline();
        helper.domain = args.getDomain();
        helper.validate();
        return helper;
    }

    private void validate() {
        if (LocalFileStorageType.TYPE.equals(this.fileStorage.getType())) {
            return; // 本地存储不需要校验
        }
        if (StringUtils.isEmpty(this.accessKey)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.accessKey");
        }
        if (StringUtils.isEmpty(this.accessSecret)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.accessSecret");
        }
        if (StringUtils.isEmpty(this.endpoint)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.endpoint");
        }
        if (StringUtils.isEmpty(this.region)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.region");
        }
        if (StringUtils.isEmpty(this.bucket)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.bucket");
        }
        if (StringUtils.isEmpty(this.domain)) {
            throw CommonErrorCode.NOT_EMPTY.exception("fileStorage.domain");
        }
    }

    public void reloadClient() {
        fileStorage.reloadClient(this.endpoint, this.region, this.accessKey, this.accessSecret);
    }

    public boolean exists(String thumbnailPath) {
        StorageExistArgs args = StorageExistArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .path(thumbnailPath)
                .build();
        return fileStorage.exists(args);
    }

    public InputStream read(String path) {
        StorageReadArgs storageReadArgs = StorageReadArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .path(path)
                .build();
        return fileStorage.read(storageReadArgs);
    }

    public void write(String path, File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            write(path, is, file.length());
        }
    }

    public void write(String path, byte[] bytes) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            write(path, is, bytes.length);
        }
    }

    public void write(String path, InputStream is, long length) {
        StorageWriteArgs args = StorageWriteArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .path(path)
                .inputStream(is)
                .length(length)
                .build();
        this.fileStorage.write(args);
    }

    public List<String> listObjects(String prefix) {
        return this.listObjects(prefix, 10);
    }

    public List<String> listObjects(String prefix, int count) {
        StorageListArgs storageListArgs = StorageListArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .prefix(prefix)
                .maxKeys(count)
                .build();
        return this.fileStorage.list(storageListArgs);
    }

    public void remove(String path) {
        StorageRemoveArgs args = StorageRemoveArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .path(path)
                .build();
        this.fileStorage.remove(args);
    }

    public void removeByPrefix(String prefix) {
        StorageListArgs storageListArgs = StorageListArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .region(this.region)
                .prefix(prefix)
                .maxKeys(100)
                .build();
        List<String> list = this.fileStorage.list(storageListArgs);
        for (String path : list) {
            StorageRemoveArgs args = StorageRemoveArgs.builder()
                    .accessKey(this.accessKey)
                    .accessSecret(this.accessSecret)
                    .bucket(this.bucket)
                    .endpoint(this.endpoint)
                    .region(this.region)
                    .path(path)
                    .build();
            this.fileStorage.remove(args);
        }
    }
}
