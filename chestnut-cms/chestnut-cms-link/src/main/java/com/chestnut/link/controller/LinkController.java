package com.chestnut.link.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.dto.LinkDTO;
import com.chestnut.link.permission.FriendLinkPriv;
import com.chestnut.link.service.ILinkService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 友情链接前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/link")
@RequiredArgsConstructor
public class LinkController extends BaseRestController {

	private final ISiteService siteService;

	private final ILinkService linkService;

	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam("groupId") @Min(1) Long groupId,
			@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsLink> page = this.linkService.lambdaQuery().eq(CmsLink::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), CmsLink::getName, query)
				.orderByAsc(CmsLink::getSortFlag)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		if (page.getRecords().size() > 0) {
			page.getRecords().forEach(link -> {
				if (StringUtils.isNotEmpty(link.getLogo())) {
					link.setSrc(InternalUrlUtils.getActualPreviewUrl(link.getLogo()));
				}
			});
		}
		return this.bindDataTable(page);
	}

	@Log(title = "新增友链", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Add)
	@PostMapping
	public R<?> add(@RequestBody  @Validated LinkDTO dto) {
		CmsLink link = new CmsLink();
		BeanUtils.copyProperties(dto, link, "siteId", "sortFlag");

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		link.setSiteId(site.getSiteId());
		link.setLinkId(IdUtils.getSnowflakeId());
		link.setSortFlag(SortUtils.getDefaultSortValue());
		link.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkService.save(link);
		return R.ok();
	}

	@Log(title = "编辑友链", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { FriendLinkPriv.Add, FriendLinkPriv.Edit } )
	@PutMapping
	public R<String> edit(@RequestBody  @Validated LinkDTO dto) {
		CmsLink link = new CmsLink();
		BeanUtils.copyProperties(dto, link, "siteId", "groupId");
		link.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkService.updateById(link);
		return R.ok();
	}

	@Log(title = "删除友链", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Delete)
	@DeleteMapping
	public R<String> remove(@RequestBody @Validated @NotEmpty List<LinkDTO> dtoList) {
		List<Long> linkIds = dtoList.stream().map(LinkDTO::getLinkId).toList();
		this.linkService.removeByIds(linkIds);
		return R.ok();
	}
}
