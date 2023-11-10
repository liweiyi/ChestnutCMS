package com.chestnut.contentcore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsContentRela;
import com.chestnut.contentcore.mapper.CmsContentRelaMapper;
import com.chestnut.contentcore.service.IContentRelaService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关联内容管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/content/rela")
public class ContentRelaController extends BaseRestController {

	private final IContentRelaService contentRelaService;

	private final CmsContentRelaMapper contentRelaMapper;

	@GetMapping
	public R<?> getRelaContents(@RequestParam Long contentId, @RequestParam(required = false) String title) {
		PageRequest pr = this.getPageRequest();
		Page<CmsContent> page = contentRelaMapper.selectRelaContents(new Page<>(pr.getPageNumber(), pr.getPageSize(), true),
				contentId, title);
		return this.bindDataTable(page);
	}

	@PostMapping
	public R<?> addRelaContents(@RequestParam Long contentId, @RequestBody List<Long> relaContentIds) {
		List<Long> contentIds = contentRelaMapper.selectList(new LambdaQueryWrapper<CmsContentRela>()
				.eq(CmsContentRela::getContentId, contentId))
				.stream().map(CmsContentRela::getRelaContentId).toList();
		String operator = StpAdminUtil.getLoginUser().getUsername();
		List<CmsContentRela> relaContents = relaContentIds.stream()
				.filter(id -> !contentIds.contains(id))
				.map(relaContentId -> {
					CmsContentRela rela = new CmsContentRela();
					rela.setRelaId(IdUtils.getSnowflakeId());
					rela.setContentId(contentId);
					rela.setRelaContentId(relaContentId);
					rela.createBy(operator);
					return rela;
				}).toList();
		this.contentRelaService.saveBatch(relaContents);
		return R.ok();
	}

	@DeleteMapping
	public R<?> deleteRelaContents(@RequestParam Long contentId, @RequestBody @NotEmpty List<Long> relaContentIds) {
		this.contentRelaService.remove(new LambdaQueryWrapper<CmsContentRela>()
						.eq(CmsContentRela::getContentId, contentId)
						.in(CmsContentRela::getRelaContentId, relaContentIds));
		return R.ok();
	}
}
