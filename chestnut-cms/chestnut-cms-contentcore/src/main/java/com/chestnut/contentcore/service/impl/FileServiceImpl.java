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
package com.chestnut.contentcore.service.impl;

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.FileAddDTO;
import com.chestnut.contentcore.domain.vo.FileVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.AllowUploadFileType;
import com.chestnut.contentcore.service.IFileService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

	/**
	 * 可在线编辑的文件类型
	 */
	public static final List<String> EDITABLE_FILE_TYPE = List.of("txt", "shtml", "html", "htm", "js", "css");
	
	private final IPublishPipeService publishPipeService;
	
	@Override
	public List<TreeNode<String>> getSiteDirectoryTreeData(CmsSite site) {
		List<TreeNode<String>> list = new ArrayList<>();
		// 资源目录
		File siteResourceRoot = new File(SiteUtils.getSiteResourceRoot(site));
		FileExUtils.mkdirs(siteResourceRoot.getAbsolutePath());

		list.add(new TreeNode<>(siteResourceRoot.getName(), "", siteResourceRoot.getName(), true));
		// 发布通道目录
		this.publishPipeService.getAllPublishPipes(site.getSiteId()).forEach(publishPipe -> {
			File siteRoot = new File(SiteUtils.getSiteRoot(site, publishPipe.getCode()));
			FileExUtils.mkdirs(siteRoot.getAbsolutePath());
			list.add(new TreeNode<>(siteRoot.getName(), "", siteRoot.getName(), true));
		});
		list.forEach(this::loadChildrenDirectories);
		return list;
	}
	
	private void loadChildrenDirectories(TreeNode<String> node) {
		File root = new File(CMSConfig.getResourceRoot() + node.getId());
		File[] listFiles = root.listFiles();
		if (Objects.isNull(listFiles)) {
			return;
		}
        for (File file : listFiles) {
			if (file.isDirectory()) {
				String id = node.getId() + StringUtils.SLASH + file.getName();
				if (Objects.isNull(node.getChildren())) {
					node.setChildren(new ArrayList<>());
				}
				TreeNode<String> child = new TreeNode<>(id, node.getId(), file.getName(), false);
				node.getChildren().add(child);
				loadChildrenDirectories(child);
			}
		}
	}

	@Override
	public R<List<FileVO>> getSiteFileList(CmsSite site, String dirPath, String filterFilename) {
    	String path = FileExUtils.normalizePath(dirPath);
    	String root = CMSConfig.getResourceRoot();
    	File file = new File(root + path);
    	if (!file.exists() || !file.isDirectory()) {
    		return R.fail("目录参数错误：" + dirPath);
    	}
		List<FileVO> list = new ArrayList<>();
    	File[] listFiles;
    	if (StringUtils.isEmpty(dirPath) || dirPath.equals("/")) {
    		// 返回站点资源及发布通道目录
    		List<CmsPublishPipe> allPublishPipes = this.publishPipeService.getAllPublishPipes(site.getSiteId());
    		listFiles = new File[allPublishPipes.size() + 1];
    		listFiles[0] = new File(SiteUtils.getSiteResourceRoot(site));
    		for (int i = 0; i < allPublishPipes.size(); i++) {
				listFiles[i + 1] = new File(SiteUtils.getSiteRoot(site, allPublishPipes.get(i).getCode()));
			}
    	} else {
    		// 返回指定目录文件
        	listFiles = file.listFiles((dir, name) -> {
				if (StringUtils.isNotEmpty(filterFilename)) {
					return name.contains(filterFilename);
				}
				return true;
			});
    	}
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length &&  i < 1000; i++) {
				// 最多显示1000个文件
				File f = listFiles[i];
				String filePath = FileExUtils.normalizePath(f.getAbsolutePath());
				FileVO vo = new FileVO();
				vo.setFilePath(filePath.substring(root.length()));
				vo.setFileName(f.getName());
				vo.setIsDirectory(f.isDirectory());
				vo.setFileSize(f.length());
				vo.setModifyTime(DateUtils.epochMilliToLocalDateTime(f.lastModified()));
				if (f.isFile()) {
					String fileType = FileExUtils.getExtension(f.getName());
					vo.setCanEdit(FileServiceImpl.EDITABLE_FILE_TYPE.contains(fileType));
				}
				list.add(vo);
			}
		}
		list.sort((f1, f2) -> {
			if (f2.getIsDirectory() && !f1.getIsDirectory()) {
				return 1;
			} else if (f1.getIsDirectory() && !f2.getIsDirectory()) {
				return -1;
			}
			return f1.getFileName().compareTo(f2.getFileName());
		});
		return R.ok(list);
	}

	@Override
	public void renameFile(CmsSite site, String filePath, String rename) throws IOException {
    	String path = FileExUtils.normalizePath(filePath);
    	if (path.startsWith("/")) {
    		path = path.substring(1);
    	}
    	this.checkSiteDirectory(site, filePath);
    	
    	String root = CMSConfig.getResourceRoot();
    	File file = new File(root + path);
    	Assert.isTrue(file.exists(), ContentCoreErrorCode.FILE_NOT_EXIST::exception);
    	
    	String dest = root + StringUtils.substringBeforeLast(path, "/") + "/" + rename;
    	File destFile = new File(dest);
    	Assert.isFalse(destFile.exists(), ContentCoreErrorCode.FILE_ALREADY_EXISTS::exception);

    	if (file.isDirectory()) {
			FileUtils.moveDirectory(file, destFile);
		} else {
        	this.checkFileType(rename);
			FileUtils.moveFile(file, destFile);
		}
	}

	@Override
	public void addFile(CmsSite site, FileAddDTO dto) throws IOException {
		String dir = dto.getDir();
    	dir = FileExUtils.normalizePath(dir);
    	if (dir.startsWith("/")) {
    		dir = dir.substring(1);
    	}
    	if (!dir.endsWith("/")) {
    		dir += "/";
    	}
    	this.checkSiteDirectory(site, dir);
    	if (!dto.getIsDirectory()) {
    		this.checkFileType(dto.getFileName());
    	}
    	
    	String root = CMSConfig.getResourceRoot();
    	File file = new File(root + dir + dto.getFileName());
    	Assert.isFalse(file.exists(), ContentCoreErrorCode.FILE_ALREADY_EXISTS::exception);

    	if (dto.getIsDirectory()) {
			FileExUtils.mkdirs(file.getAbsolutePath());
    	} else {
			FileExUtils.mkdirs(file.getParentFile().getAbsolutePath());
    		FileUtils.writeStringToFile(file, StringUtils.EMPTY, StandardCharsets.UTF_8);
    	}
	}

	@Override
	public String readFile(CmsSite site, String filePath) throws IOException {
    	String path = FileExUtils.normalizePath(filePath);
    	if (path.startsWith("/")) {
    		path = path.substring(1);
    	}
		this.checkSiteDirectory(site, path);
    	if (!EDITABLE_FILE_TYPE.contains(FileExUtils.getExtension(path))) {
    		throw ContentCoreErrorCode.NOT_EDITABLE_FILE.exception();
    	}
    	String root = CMSConfig.getResourceRoot();
    	File file = new File(root + path);
    	Assert.isTrue(file.exists(), ContentCoreErrorCode.FILE_NOT_EXIST::exception);

    	return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

	@Override
	public void editFile(CmsSite site, String filePath, String fileContent) throws IOException {
    	String path = FileExUtils.normalizePath(filePath);
    	if (path.startsWith("/")) {
    		path = path.substring(1);
    	}
		this.checkSiteDirectory(site, path);
    	if (!EDITABLE_FILE_TYPE.contains(FileExUtils.getExtension(path))) {
    		throw ContentCoreErrorCode.NOT_EDITABLE_FILE.exception();
    	}
    	String root = CMSConfig.getResourceRoot();
    	File file = new File(root + path);
    	Assert.isTrue(file.exists(), ContentCoreErrorCode.FILE_NOT_EXIST::exception);
    	
    	FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8);
	}

	@Override
	public void deleteFiles(CmsSite site, String[] filePathArr) throws IOException {
    	String root = CMSConfig.getResourceRoot();
		for (String filePath : filePathArr) {
			String path = FileExUtils.normalizePath(filePath);
	    	if (path.startsWith("/")) {
	    		path = path.substring(1);
	    	}
	    	this.checkSiteDirectory(site, path);
	    	File file = new File(root + path);
	    	if (file.isDirectory()) {
	    		FileUtils.deleteDirectory(file);
	    	} else {
	    		FileUtils.delete(file);
	    	}
		}
	}

	@Override
	public void uploadFile(CmsSite site, String dir, MultipartFile file) throws IOException {
		dir = FileExUtils.normalizePath(dir);
		if (dir.startsWith("/")) {
			dir = dir.substring(1);
		}
		if (!dir.endsWith("/")) {
			dir += "/";
		}
		this.checkSiteDirectory(site, dir);
		this.checkFileType(file.getOriginalFilename());
		
		String resourceRoot = CMSConfig.getResourceRoot();
		FileUtils.writeByteArrayToFile(new File(resourceRoot + dir + file.getOriginalFilename()), file.getBytes());
	}
	
	/**
	 * 校验上传文件类型
	 * 
	 * @param ext
	 */
	private void checkFileType(String ext) {
		if (AllowUploadFileType.isAllow(ext)) {
			return;
		}
		throw ContentCoreErrorCode.NOT_ALLOW_FILE_TYPE.exception(ext);
	}

	/**
	 * 判断上传目录是否是站点相关目录：站点资源目录、站点发布通道目录
	 * 
	 * @param site
	 * @param path 站点相关目录子路径，如果是目录必须以/结尾
	 * @return
	 */
	private void checkSiteDirectory(CmsSite site, String path) {
		if (path.contains("/")) {
			path = StringUtils.substringBefore(path, "/");
		}
		if (StringUtils.isNotEmpty(path)) {
			if (path.equals(site.getPath())) {
				return;
			}
			List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
			for (CmsPublishPipe publishPipe : publishPipes) {
				if (path.equals(site.getPath() + "_" + publishPipe.getCode())) {
					return;
				}
			}
		}
		throw ContentCoreErrorCode.SITE_FILE_OP_ERR.exception(path);
	}
}
