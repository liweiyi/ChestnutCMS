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
package com.chestnut.contentcore.core;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * CMS初始化器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICmsInitializer {

    /**
     * 判断指定资源是否是私有文件，私有文件不可通过preview/路径访问
     *
     * @param resourcePath 相对于资源映射根目录的请求路径，可能包含 URL 编码
     * @param resource Spring 已解析并通过基础路径校验的目标文件
     * @return 是否禁止直接预览，true 表示私有资源或不安全路径
     * @throws IOException 读取或校验资源路径失败
     */
    default boolean isPrivateResource(String resourcePath, Resource resource) throws IOException {
        return false;
    }

    /**
     * 获取禁止公开访问的站点文件、目录及文件后缀。
     * 文件和目录路径相对于站点发布目录或站点资源目录，不包含站点目录名。
     */
    List<FilePathRecord> getPrivateSitePaths();

    enum PathType {
        /** 相对站点目录的具体文件，不使用通配符。 */
        FILE,
        /** 相对站点目录的目录，包含整个子目录树。 */
        DIRECTORY,
        /** 任意目录中文件名的后缀，例如 .template.html。 */
        FILE_SUFFIX
    }

    record FilePathRecord(PathType type, String path) {}
}
