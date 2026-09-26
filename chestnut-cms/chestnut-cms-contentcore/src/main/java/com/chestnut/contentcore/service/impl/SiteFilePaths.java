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
package com.chestnut.contentcore.service.impl;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * 一次文件操作使用的站点目录授权快照。只有配置的 CMS 根目录允许解析符号链接。
 * 根目录以下须由服务端管理；路径检查不能替代对本地并发目录替换的文件系统权限限制。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
final class SiteFilePaths {

    private final Path base;
    private final List<Path> roots;

    SiteFilePaths(Path configuredRoot, List<String> rootNames) throws IOException {
        this.base = configuredRoot.toAbsolutePath().normalize().toRealPath();
        this.roots = rootNames.stream().map(name -> {
            validateName(name);
            return base.resolve(name).normalize();
        }).distinct().toList();
    }

    List<Path> roots() {
        return roots;
    }

    static void validateName(String name) {
        if (!FileExUtils.isSafeFileName(name) || name.indexOf(':') >= 0) {
            throw CommonErrorCode.INVALID_REQUEST_ARG.exception("fileName");
        }
    }

    Path resolve(String input) throws IOException {
        if (StringUtils.isBlank(input) || input.indexOf('\0') >= 0 || input.indexOf(':') >= 0) {
            throw denied();
        }
        String relative = input.replace('\\', '/');
        if (relative.startsWith("//")) {
            throw denied();
        }
        // 兼容现有接口的 /site/...，它始终是相对 CMS 根目录的路径。
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        for (String segment : relative.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                throw denied();
            }
        }
        try {
            return check(base.resolve(relative).normalize());
        } catch (InvalidPathException e) {
            throw denied();
        }
    }

    Path child(Path directory, String name) throws IOException {
        validateName(name);
        check(directory);
        Path target = directory.resolve(name).normalize();
        if (!directory.equals(target.getParent())) {
            throw denied();
        }
        return check(target);
    }

    /** 严格的根目录内相对路径，不使用文件管理接口的前导斜杠兼容规则。 */
    static String strictRelative(String input) {
        if (StringUtils.isBlank(input) || input.indexOf('\0') >= 0 || input.indexOf(':') >= 0) {
            throw denied();
        }
        String relative = input.replace('\\', '/');
        for (String segment : relative.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw denied();
            }
        }
        try {
            if (Path.of(relative).isAbsolute()) {
                throw denied();
            }
        } catch (InvalidPathException e) {
            throw denied();
        }
        return relative;
    }

    Path resolveRelative(Path root, String input) throws IOException {
        if (!roots.contains(root)) {
            throw denied();
        }
        Path target = root.resolve(strictRelative(input)).normalize();
        if (target.equals(root) || !target.startsWith(root)) {
            throw denied();
        }
        return check(target);
    }

    Path checkFile(Path target) throws IOException {
        check(target);
        protectRoot(target);
        BasicFileAttributes attrs = attributesIfExists(target);
        if (attrs != null && !attrs.isRegularFile()) {
            throw denied();
        }
        return target;
    }

    /** 先做词法授权，再检查每个已存在的路径分量；缺失的目标及祖先可用于安全新建。 */
    Path check(Path target) throws IOException {
        authorize(target);
        Path current = base;
        for (Path name : base.relativize(target)) {
            current = current.resolve(name);
            BasicFileAttributes attrs = attributesIfExists(current);
            if (attrs == null) {
                break;
            }
            checkExisting(current, attrs);
            if (!current.equals(target) && !attrs.isDirectory()) {
                throw denied();
            }
        }
        return target;
    }

    private void authorize(Path target) {
        if (!target.isAbsolute() || !target.equals(target.normalize()) || target.equals(base)
                || roots.stream().noneMatch(target::startsWith)) {
            throw denied();
        }
    }

    private void checkExisting(Path path, BasicFileAttributes attrs) throws IOException {
        if (attrs.isSymbolicLink() || (!attrs.isDirectory() && !attrs.isRegularFile())
                || !path.toRealPath().equals(path)) {
            throw denied();
        }
    }

    private BasicFileAttributes attributesIfExists(Path path) throws IOException {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, NOFOLLOW_LINKS);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    BasicFileAttributes requireExisting(Path path) throws IOException {
        check(path);
        BasicFileAttributes attrs = attributesIfExists(path);
        if (attrs == null) {
            throw ContentCoreErrorCode.FILE_NOT_EXIST.exception();
        }
        checkExisting(path, attrs);
        return attrs;
    }

    /** 列表/目录树不展示链接，包括链接形式的站点根目录。 */
    BasicFileAttributes visibleAttributes(Path path) throws IOException {
        try {
            check(path);
            BasicFileAttributes attrs = attributesIfExists(path);
            if (attrs != null) {
                checkExisting(path, attrs);
            }
            return attrs;
        } catch (GlobalException e) {
            if (e.getErrorCode() == ContentCoreErrorCode.SITE_FILE_OP_ERR) {
                return null;
            }
            throw e;
        }
    }

    void createDirectories(Path directory) throws IOException {
        check(directory);
        Path current = base;
        for (Path name : base.relativize(directory)) {
            current = current.resolve(name);
            if (attributesIfExists(current) == null) {
                try {
                    Files.createDirectory(current);
                } catch (FileAlreadyExistsException ignored) {
                    // 并发创建后仍需检查目录类型和符号链接。
                }
            }
            BasicFileAttributes attrs = Files.readAttributes(current, BasicFileAttributes.class, NOFOLLOW_LINKS);
            checkExisting(current, attrs);
            if (!attrs.isDirectory()) {
                throw denied();
            }
        }
    }

    void protectRoot(Path path) {
        authorize(path);
        if (roots.contains(path)) {
            throw denied();
        }
    }

    void checkTree(Path path) throws IOException {
        requireExisting(path);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                check(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                check(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    void deleteTree(Path path) throws IOException {
        protectRoot(path);
        check(path);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(check(file));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException {
                if (error != null) {
                    throw error;
                }
                Files.delete(check(dir));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    String relative(Path path) {
        authorize(path);
        return base.relativize(path).toString().replace('\\', '/');
    }

    private static GlobalException denied() {
        return ContentCoreErrorCode.SITE_FILE_OP_ERR.exception();
    }
}
