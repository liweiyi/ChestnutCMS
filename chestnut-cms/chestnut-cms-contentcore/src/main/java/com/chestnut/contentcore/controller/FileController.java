package com.chestnut.contentcore.controller;

import java.io.IOException;
import java.util.List;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.contentcore.util.CmsPrivUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.FileAddDTO;
import com.chestnut.contentcore.domain.dto.FileOperateDTO;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IFileService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

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
	 * @param filePath
	 * @param filename
	 * @return
	 */
	@GetMapping("/list")
	public R<?> getFileList(@RequestParam("filePath") @NotEmpty String filePath,
			@RequestParam("fileName") String filename) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return this.fileService.getSiteFileList(site, filePath, filename);
	}

	/**
	 * 获取目录树数据
	 * 
	 * @return
	 */
	@GetMapping("/directoryTreeData")
	public R<?> getDirectoryTree() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<TreeNode<String>> list = this.fileService.getSiteDirectoryTreeData(site);
		return R.ok(list);
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
	@Log(title = "读取文", businessType = BusinessType.OTHER)
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
