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
package com.chestnut.contentcore.core;

import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.SiteUtils;
import jodd.io.ZipBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 站点主题导出上下文
 *
 * <p>
 * 保存导出过程中的临时数据供各模块使用<br/>
 * 导出文件可保存至siteResourceRoot/_export/theme/目录下，数据库数据默认位于db目录下<br/>
 * theme目录下的导出临时文件会在导出逻辑最后按目录名打包<br/>
 * 目前已知目录：<br/>
 * 站点文件：wwwroot/<br/>
 * 数据库文件：db/
 * </p>
 */
@Slf4j
@Getter
@Setter
public class SiteExportContext implements ISiteThemeContext {

    /**
     * 导出临时目录
     */
    static final String ExportDir = "_export/theme/";

    /**
     * 引用资源IDS
     */
    private Set<Long> resourceIds = new HashSet<>();

    /**
     * 导出内容ID列表
     */
    private Set<Long> contentIds = new HashSet<>();

    private final CmsSite site;

    /**
     * 是否在打包后清理临时目录
     */
    private boolean clearTempFile = true;

    public SiteExportContext(CmsSite site) {
        this.site = site;
    }

    public void createZipFile(String zipPath) throws IOException {
        String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        String zipFile = siteResourceRoot + zipPath;
        ZipBuilder zipBuilder = ZipBuilder.createZipFile(new File(zipFile));
        File exportDir = new File(siteResourceRoot + ExportDir);
        File[] files = exportDir.listFiles();
        if (Objects.nonNull(files)) {
            for (File f : files) {
                zipBuilder.add(f).path(f.getName()).recursive().save();
            }
        }
        zipBuilder.toZipFile();
        if (clearTempFile) {
            this.clearTempFiles();
        }
    }

    /**
     * 保存文件到${SiteDirPath}目录
     *
     * @param source 源文件
     * @param dest 目标路径，项目资源根目录（resourceRoot）
     */
    public void saveFile(File source, String dest) {
        dest = ExportDir + SiteDirPath + dest;
        File destFile = new File(SiteUtils.getSiteResourceRoot(site) + dest);
        try {
            if (source.isDirectory()) {
                FileUtils.copyDirectory(source, destFile);
            } else {
                FileUtils.copyFile(source, destFile);
            }
        } catch (IOException e) {
            log.error("Copy directory/file {} to {} failed!", source.getAbsolutePath(), destFile.getAbsolutePath(), e);
        }
    }

    public void saveData(String tableName, String jsonData) {
        saveData(tableName, jsonData, 1);
    }

    public void saveData(String tableName, String jsonData, int index) {
        String path = ExportDir + DataDirPath + tableName + SPLITER + index + ".json";
        File f = new File(SiteUtils.getSiteResourceRoot(site) + path);
        try {
            FileUtils.writeStringToFile(f, jsonData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Write content to file `{}` failed!", f.getAbsolutePath(), e);
        }
    }

    public void clearTempFiles() throws IOException {
        String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        FileUtils.deleteDirectory(new File(siteResourceRoot + ExportDir));
    }
}