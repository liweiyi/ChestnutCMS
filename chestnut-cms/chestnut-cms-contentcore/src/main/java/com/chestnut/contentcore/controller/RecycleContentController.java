package com.chestnut.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容回收站管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
	type = AdminUserType.TYPE,
	value = { ContentCorePriv.ContentView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
	mode = SaMode.AND
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content/recycle")
public class RecycleContentController extends BaseRestController {

	private final ISiteService siteService;

	private final CmsContentMapper contentMapper;

	private final IContentService contentService;

	@GetMapping
	public R<?> getRecycleContentList(@RequestParam(name = "catalogId", required = false) Long catalogId,
									  @RequestParam(name = "contentType", required = false) String contentType,
									  @RequestParam(name = "title", required = false) String title,
									  @RequestParam(name = "status", required = false) String status) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());

		Page<CmsContent> page = this.contentMapper.selectPageWithLogicDel(new Page<>(pr.getPageNumber(), pr.getPageSize(), true),
				site.getSiteId(), catalogId, contentType, status, title);
		return this.bindDataTable(page);
	}

	@Log(title = "恢复回收站内容", businessType = BusinessType.INSERT)
	@PostMapping("/recover")
	public R<?> recoverContent(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.recoverContents(backupIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@Log(title = "删除回收站内容", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteRecycleContents(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.deleteRecycleContents(backupIds);
		return R.ok();
	}
}
