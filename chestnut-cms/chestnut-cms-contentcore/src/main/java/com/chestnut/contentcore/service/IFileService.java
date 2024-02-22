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
package com.chestnut.contentcore.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.FileAddDTO;
import com.chestnut.contentcore.domain.vo.FileVO;

public interface IFileService {

	/**
	 * 获取站点目录树
	 * 
	 * @param site
	 * @return
	 */
	List<TreeNode<String>> getSiteDirectoryTreeData(CmsSite site);

	/**
	 * 获取站点指定目录下的文件
	 * 
	 * @param site
	 * @param dirPath
	 * @param filterFilename
	 * @return
	 */
	R<List<FileVO>> getSiteFileList(CmsSite site, String dirPath, String filterFilename);

	/**
	 * 重命名文件
	 * 
	 * @param site
	 * @param filePath
	 * @param rename
	 * @return
	 * @throws IOException 
	 */
	void renameFile(CmsSite site, String filePath, String rename) throws IOException;

	/**
	 * 新建文件
	 * 
	 * @param site
	 * @param dto
	 * @return
	 * @throws IOException 
	 */
	void addFile(CmsSite site, FileAddDTO dto) throws IOException;

	/**
	 * 读取文件内容
	 * 
	 * @param site
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	String readFile(CmsSite site, String filePath) throws IOException;

	/**
	 * 修改文件内容
	 * 
	 * @param site
	 * @param filePath
	 * @param fileContent
	 * @return
	 * @throws IOException 
	 */
	void editFile(CmsSite site, String filePath, String fileContent) throws IOException;

	/**
	 * 删除文件
	 * 
	 * @param filePathArr
	 * @return
	 * @throws IOException 
	 */
	void deleteFiles(CmsSite site, String[] filePathArr) throws IOException;
	
	/**
	 * 上传文件到站点指定目录
	 * 
	 * @param site
	 * @param dir
	 * @param file
	 * @throws IOException 
	 */
	void uploadFile(CmsSite site, String dir, MultipartFile file) throws IOException;
}
