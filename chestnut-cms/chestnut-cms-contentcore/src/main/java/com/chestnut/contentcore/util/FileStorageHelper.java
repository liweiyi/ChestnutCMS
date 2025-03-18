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

import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.storage.StorageListArgs;
import com.chestnut.common.storage.StorageReadArgs;
import com.chestnut.common.storage.StorageWriteArgs;
import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.FileStorageArgsProperty;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        helper.pipeline = args.getPipeline();
        helper.domain = args.getDomain();
        return helper;
    }

    public InputStream read(String path) {
        StorageReadArgs storageReadArgs = StorageReadArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .path(path)
                .build();
        return fileStorage.read(storageReadArgs);
    }

    public void write(String path, byte[] bytes) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            write(path, is);
        }
    }

    public void write(String path, InputStream is) {
        StorageWriteArgs args = StorageWriteArgs.builder()
                .accessKey(this.accessKey)
                .accessSecret(this.accessSecret)
                .bucket(this.bucket)
                .endpoint(this.endpoint)
                .path(path)
                .inputStream(is)
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
                .prefix(prefix)
                .maxKeys(count)
                .build();
        return this.fileStorage.list(storageListArgs);
    }
}
