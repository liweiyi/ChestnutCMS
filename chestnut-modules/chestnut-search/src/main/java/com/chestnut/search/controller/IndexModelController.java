package com.chestnut.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.IndexModel;
import com.chestnut.search.domain.dto.SearchModelDTO;
import com.chestnut.search.service.IIndexModelService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/search/model")
public class IndexModelController extends BaseRestController {

	private final IIndexModelService indexModelService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<IndexModel> page = this.indexModelService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), IndexModel::getName, query).or()
				.like(StringUtils.isNotEmpty(query), IndexModel::getCode, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "新增索引模型", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addIndexModel(@RequestBody @Validated SearchModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.indexModelService.addIndexModel(dto);
		return R.ok();
	}

	@Log(title = "删除索引模型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteIndexModel(@RequestBody @NotEmpty List<Long> modelIds) {
		this.indexModelService.deleteIndexModel(modelIds);
		return R.ok();
	}
}