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
package com.chestnut.contentcore.util;

import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.impl.ResourceType_File;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.FileStorageArgsProperty;

import java.util.List;

public class ResourceUtils {

    /**
     * 通过文件后缀名获取对应资源类型，由于文件资源包含所有文件类型，再获取到超过1个资源类型时将文件资源类型移除再获取。
     *
     * @param suffix
     * @return
     */
    public static IResourceType getResourceTypeBySuffix(String suffix) {
        List<IResourceType> list = ContentCoreUtils.getResourceTypes().stream().filter(rt -> rt.check(suffix)).toList();
        if (list.size() > 1) {
            return list.stream().filter(rt -> !rt.getId().equals(ResourceType_File.ID)).findFirst().get();
        }
        return list.get(0);
    }

    public static String getResourcePrefix(String storageType, CmsSite site, boolean isPreview) {
        if (LocalFileStorageType.TYPE.equals(storageType)) {
            return SiteUtils.getResourcePrefix(site, isPreview);
        }
        FileStorageArgsProperty.FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
        if (fileStorageArgs != null && StringUtils.isNotEmpty(fileStorageArgs.getDomain())) {
            String domain = fileStorageArgs.getDomain();
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain;
        }
        // 无法获取到对象存储访问地址时默认使用站点资源域名
        return site.getResourceUrl();
    }
}
