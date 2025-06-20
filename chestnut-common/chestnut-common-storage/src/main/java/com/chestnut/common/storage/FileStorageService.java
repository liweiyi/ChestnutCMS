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
package com.chestnut.common.storage;

import com.chestnut.common.storage.exception.StorageErrorCode;
import com.chestnut.common.utils.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * FileStorageService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Map<String, IFileStorageType> fileStorageTypes;

    public IFileStorageType getFileStorageType(String type) {
        IFileStorageType fileStorageType = fileStorageTypes.get(IFileStorageType.BEAN_NAME_PREIFX + type);
        Assert.notNull(fileStorageType, () -> StorageErrorCode.UNSUPPORTED_STORAGE_TYPE.exception(type));
        return fileStorageType;
    }

    public Optional<IFileStorageType> optFileStorageType(String type) {
        IFileStorageType fileStorageType = fileStorageTypes.get(IFileStorageType.BEAN_NAME_PREIFX + type);
        return Optional.ofNullable(fileStorageType);
    }
}
