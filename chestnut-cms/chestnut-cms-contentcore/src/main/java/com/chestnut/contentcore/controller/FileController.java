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
package com.chestnut.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.FileAddDTO;
import com.chestnut.contentcore.domain.dto.FileOperateDTO;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IFileService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.FileView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/file")
public class FileController extends BaseRestController {

	private final ISiteService siteService;

	private final IFileService fileService;

	/**
	 * 查询文件列表
	 *
	 * @param filePath 文件目录路径
	 * @param fileName 文件名
	 */
	@GetMapping("/list")
	public R<?> getFileList(@RequestParam @NotEmpty String filePath,
							@RequestParam(required = false, defaultValue = "") String fileName) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return this.fileService.getSiteFileList(site, filePath, fileName);
	}

	/**
	 * 获取目录树数据
	 */
	@GetMapping("/directoryTreeData")
	public R<?> getDirectoryTree() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<TreeNode<String>> list = this.fileService.getSiteDirectoryTreeData(site);
		return R.ok(Map.of("tree", list, "resourceRoot", CMSConfig.getResourceRoot()));
	}

	/**
	 * 重命名文件
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "文件重命名", businessType = BusinessType.UPDATE)
	@PostMapping("/rename")
	public R<?> renameFile(@RequestBody @Validated FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.renameFile(site, dto.getFilePath(), dto.getRename());
		return R.ok();
	}

	/**
	 * 新建文件
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "新建文件", businessType = BusinessType.UPDATE)
	@PostMapping("/add")
	public R<?> addFile(@RequestBody @Validated FileAddDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.addFile(site, dto);
		return R.ok();
	}

	/**
	 * 上传文件
	 *
	 * @return
	 * @throws IOException
	 */
	@Log(title = "上传文件", businessType = BusinessType.UPDATE)
	@PostMapping("/upload")
	public R<?> uploadFile(@RequestParam("dir") @NotEmpty String dir, @RequestParam("file") MultipartFile multipartFile)
			throws IOException {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.uploadFile(site, dir, multipartFile);
		return R.ok();
	}

	/**
	 * 读取文件内容
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "读取文件", businessType = BusinessType.OTHER)
	@PostMapping("/read")
	public R<?> readFile(@RequestBody @Validated FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return R.ok(this.fileService.readFile(site, dto.getFilePath()));
	}

	/**
	 * 修改文件内容
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "修改文件", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	public R<?> editFile(@RequestBody @Validated FileOperateDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.fileService.editFile(site, dto.getFilePath(), dto.getFileContent());
		return R.ok();
	}

	/**
	 * 删除文件
	 *
	 * @param dtoList
	 * @return
	 * @throws IOException
	 */
	@Log(title = "删除文件", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> deleteFile(@RequestBody @NotEmpty List<FileOperateDTO> dtoList) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String[] filePathArr = dtoList.stream().map(FileOperateDTO::getFilePath).toArray(String[]::new);
		this.fileService.deleteFiles(site, filePathArr);
		return R.ok();
	}
}
