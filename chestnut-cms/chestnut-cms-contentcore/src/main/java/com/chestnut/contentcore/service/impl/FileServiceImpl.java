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

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.FileAddDTO;
import com.chestnut.contentcore.domain.vo.FileVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.AllowUploadFileType;
import com.chestnut.contentcore.fixed.config.OnlineEditableFileType;
import com.chestnut.contentcore.service.IFileService;
import com.chestnut.contentcore.service.IPublishPipeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.*;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final IPublishPipeService publishPipeService;

    private SiteFilePaths paths(CmsSite site) throws IOException {
        List<String> roots = new ArrayList<>();
        roots.add(site.getPath());
        // 发布通道启停只影响发布，文件管理允许访问本站所有通道。
        publishPipeService.getAllPublishPipes(site.getSiteId())
                .forEach(pipe -> roots.add(site.getPath() + "_" + pipe.getCode()));
        return new SiteFilePaths(Path.of(CMSConfig.getResourceRoot()), roots);
    }

    @Override
    public List<TreeNode<String>> getSiteDirectoryTreeData(CmsSite site) {
        try {
            SiteFilePaths paths = paths(site);
            List<TreeNode<String>> list = new ArrayList<>();
            for (Path root : paths.roots()) {
                if (Files.exists(root, NOFOLLOW_LINKS) && paths.visibleAttributes(root) == null) {
                    continue;
                }
                paths.createDirectories(root);
                String name = root.getFileName().toString();
                TreeNode<String> node = new TreeNode<>(paths.relative(root), "", name, true);
                loadChildrenDirectories(paths, root, node);
                list.add(node);
            }
            return list;
        } catch (IOException e) {
            throw CommonErrorCode.SYSTEM_ERROR.exception(e);
        }
    }

    private void loadChildrenDirectories(SiteFilePaths paths, Path directory, TreeNode<String> node)
            throws IOException {
        paths.check(directory);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path entry : entries) {
                if (StringUtils.isSystemDir(entry.toFile())) {
                    continue;
                }
                BasicFileAttributes attrs = paths.visibleAttributes(entry);
                if (attrs == null || !attrs.isDirectory()) {
                    continue;
                }
                TreeNode<String> child = new TreeNode<>(paths.relative(entry), node.getId(),
                        entry.getFileName().toString(), false);
                loadChildrenDirectories(paths, entry, child);
                if (node.getChildren() == null) {
                    node.setChildren(new ArrayList<>());
                }
                node.getChildren().add(child);
            }
        }
    }

    @Override
    public R<List<FileVO>> getSiteFileList(CmsSite site, String dirPath, String filterFilename) {
        try {
            SiteFilePaths paths = paths(site);
            List<FileVO> list = new ArrayList<>();
            if ("".equals(dirPath) || "/".equals(dirPath)) {
                // 虚拟根只能枚举授权目录，不能打开 CMS 物理根目录。
                for (Path root : paths.roots()) {
                    addFileView(paths, root, list);
                    if (list.size() >= 1000) {
                        break;
                    }
                }
            } else {
                Path directory = paths.resolve(dirPath);
                if (!Files.isDirectory(directory, NOFOLLOW_LINKS)) {
                    return R.fail("目录参数错误：" + dirPath);
                }
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
                    for (Path entry : entries) {
                        if (StringUtils.isSystemDir(entry.toFile())
                                || (StringUtils.isNotEmpty(filterFilename)
                                && !entry.getFileName().toString().contains(filterFilename))) {
                            continue;
                        }
                        addFileView(paths, entry, list);
                        if (list.size() >= 1000) {
                            break;
                        }
                    }
                }
            }
            list.sort(Comparator.comparing(FileVO::getIsDirectory).reversed()
                    .thenComparing(FileVO::getFileName));
            return R.ok(list);
        } catch (IOException e) {
            throw CommonErrorCode.SYSTEM_ERROR.exception(e);
        }
    }

    private void addFileView(SiteFilePaths paths, Path file, List<FileVO> list) throws IOException {
        BasicFileAttributes attrs = paths.visibleAttributes(file);
        if (attrs == null) {
            return;
        }
        FileVO vo = new FileVO();
        vo.setFilePath(paths.relative(file));
        vo.setFileName(file.getFileName().toString());
        vo.setIsDirectory(attrs.isDirectory());
        vo.setFileSize(attrs.size());
        vo.setModifyTime(DateUtils.epochMilliToLocalDateTime(attrs.lastModifiedTime().toMillis()));
        if (attrs.isRegularFile()) {
            vo.setCanEdit(OnlineEditableFileType.getAllowedExtensions().contains(FileExUtils.getExtension(vo.getFileName())));
        }
        list.add(vo);
    }

    @Override
    public void renameFile(CmsSite site, String filePath, String rename) throws IOException {
        SiteFilePaths.validateName(rename);
        SiteFilePaths paths = paths(site);
        Path source = paths.resolve(filePath);
        paths.protectRoot(source);
        paths.checkTree(source);
        if (StringUtils.isNotEmpty(FilenameUtils.getExtension(rename))) {
            checkFileType(rename);
        }
        Path target = paths.child(source.getParent(), rename);
        try {
            // 不使用 REPLACE_EXISTING，也不通过复制/删除退化为跨目录移动。
            Files.move(paths.check(source), paths.check(target));
        } catch (FileAlreadyExistsException e) {
            throw ContentCoreErrorCode.FILE_ALREADY_EXISTS.exception();
        }
    }

    @Override
    public void addFile(CmsSite site, FileAddDTO dto) throws IOException {
        if (dto == null || dto.getIsDirectory() == null) {
            throw CommonErrorCode.INVALID_REQUEST_ARG.exception("isDirectory");
        }
        SiteFilePaths.validateName(dto.getFileName());
        SiteFilePaths paths = paths(site);
        Path parent = paths.resolve(dto.getDir());
        Path target = paths.child(parent, dto.getFileName());
        if (!dto.getIsDirectory()) {
            checkFileType(dto.getFileName());
        }
        paths.createDirectories(parent);
        paths.check(target);
        try {
            if (dto.getIsDirectory()) {
                Files.createDirectory(target);
            } else {
                // CREATE_NEW 原子保证不覆盖；包括并发创建的文件或失效符号链接。
                Files.createFile(target);
            }
        } catch (FileAlreadyExistsException e) {
            throw ContentCoreErrorCode.FILE_ALREADY_EXISTS.exception();
        }
    }

    @Override
    public String readFile(CmsSite site, String filePath) throws IOException {
        SiteFilePaths paths = paths(site);
        Path file = paths.resolve(filePath);
        requireEditableFile(paths, file);
        try (var input = Files.newInputStream(file, READ, NOFOLLOW_LINKS)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public void editFile(CmsSite site, String filePath, String fileContent) throws IOException {
        SiteFilePaths paths = paths(site);
        Path file = paths.resolve(filePath);
        requireEditableFile(paths, file);
        byte[] content = (fileContent == null ? StringUtils.EMPTY : fileContent).getBytes(StandardCharsets.UTF_8);
        // 编辑只允许已存在的普通文件，不隐式创建。
        try (var output = Files.newOutputStream(file, WRITE, TRUNCATE_EXISTING, NOFOLLOW_LINKS)) {
            output.write(content);
        }
    }

    private void requireEditableFile(SiteFilePaths paths, Path file) throws IOException {
        if (!OnlineEditableFileType.getAllowedExtensions().contains(FilenameUtils.getExtension(file.getFileName().toString()))) {
            throw ContentCoreErrorCode.NOT_EDITABLE_FILE.exception();
        }
        if (!paths.requireExisting(file).isRegularFile()) {
            throw ContentCoreErrorCode.NOT_EDITABLE_FILE.exception();
        }
    }

    @Override
    public void deleteFiles(CmsSite site, String[] filePathArr) throws IOException {
        if (filePathArr == null || filePathArr.length == 0) {
            throw CommonErrorCode.INVALID_REQUEST_ARG.exception("filePath");
        }
        SiteFilePaths paths = paths(site);
        List<Path> targets = new ArrayList<>();
        // 所有目标及其子树先校验，拒绝请求不能导致前面的合法文件被删除。
        for (String filePath : filePathArr) {
            Path target = paths.resolve(filePath);
            paths.protectRoot(target);
            paths.checkTree(target);
            targets.add(target);
        }
        List<Path> distinct = targets.stream().distinct().toList();
        for (Path target : distinct) {
            if (distinct.stream().noneMatch(other -> !other.equals(target) && target.startsWith(other))) {
                paths.deleteTree(target);
            }
        }
    }

    @Override
    public void uploadFile(CmsSite site, String dir, MultipartFile file) throws IOException {
        if (file == null) {
            throw CommonErrorCode.INVALID_REQUEST_ARG.exception("file");
        }
        SiteFilePaths.validateName(file.getOriginalFilename());
        SiteFilePaths paths = paths(site);
        Path parent = paths.resolve(dir);
        Path target = paths.child(parent, file.getOriginalFilename());
        checkFileType(file.getOriginalFilename());
        paths.createDirectories(parent);
        paths.check(target);
        // 保留覆盖普通同名文件的上传语义，但不能跟随文件链接。
        try (var input = file.getInputStream();
             var output = Files.newOutputStream(target, CREATE, WRITE, TRUNCATE_EXISTING, NOFOLLOW_LINKS)) {
            input.transferTo(output);
        }
    }

    private void checkFileType(String name) {
        if (!AllowUploadFileType.isAllow(name)) {
            throw ContentCoreErrorCode.NOT_ALLOW_FILE_TYPE.exception(name);
        }
    }
}
