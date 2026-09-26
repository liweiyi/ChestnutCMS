package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.ICmsInitializer;
import com.chestnut.contentcore.fixed.config.TemplateSuffix;
import org.apache.commons.lang3.Strings;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 内容核心初始化器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class ContentCoreCmsInitializer implements ICmsInitializer {

    @Override
    public boolean isPrivateResource(String resourcePath, Resource resource) throws IOException {
        // 使用解析后的文件路径，不直接匹配可能包含 URL 编码的 resourcePath。
        Path root = Path.of(CMSConfig.getResourceRoot()).toAbsolutePath().normalize();
        Path path = resource.getFile().toPath().toAbsolutePath().normalize();
        if (!path.startsWith(root)) {
            return true;
        }

        Path realRoot = root.toRealPath();
        Path realPath = path.toRealPath();
        if (!realPath.startsWith(realRoot) || !Files.isRegularFile(realPath)) {
            return true;
        }

        // 同时检查访问路径和真实路径，防止符号链接绕过目录、后缀限制。
        List<FilePathRecord> privatePaths = getPrivateSitePaths();
        return isPrivatePath(root.relativize(path), privatePaths)
                || isPrivatePath(realRoot.relativize(realPath), privatePaths);
    }

    @Override
    public List<FilePathRecord> getPrivateSitePaths() {
        return List.of(
                new FilePathRecord(PathType.FILE_SUFFIX, TemplateSuffix.getValue()),
                new FilePathRecord(PathType.DIRECTORY, ContentCoreConsts.TemplateDirectory),
                new FilePathRecord(PathType.FILE, "importTheme.zip"),
                new FilePathRecord(PathType.DIRECTORY, ContentCoreConsts.SITE_EXPORT_DIR),
                // 兼容切换导出目录前遗留的导出文件。
                new FilePathRecord(PathType.DIRECTORY, "_export/"));
    }

    private boolean isPrivatePath(Path relativePath, List<FilePathRecord> privatePaths) {
        // 首段是站点或站点发布通道目录；保留目录分隔符，避免误拦截同名前缀。
        String sitePath = relativePath.getNameCount() > 1
                ? relativePath.subpath(1, relativePath.getNameCount()).toString().replace(File.separatorChar, '/')
                : "";
        for (FilePathRecord privatePath : privatePaths) {
            boolean matches = switch (privatePath.type()) {
                case FILE -> Strings.CI.equals(sitePath, privatePath.path());
                case DIRECTORY -> {
                    String directory = privatePath.path();
                    if (directory.endsWith("/")) {
                        directory = directory.substring(0, directory.length() - 1);
                    }
                    yield Strings.CI.equals(sitePath, directory)
                            || Strings.CI.startsWith(sitePath, directory + "/");
                }
                case FILE_SUFFIX -> Strings.CI.endsWith(relativePath.getFileName().toString(), privatePath.path());
            };
            if (matches) {
                return true;
            }
        }
        return false;
    }
}
