package com.chestnut.word.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.ErrorProneWord;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IErrorProneWordService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 易错词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/errorproneword")
public class ErrorProneWordController extends BaseRestController {

	private final IErrorProneWordService errorProneWordService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<ErrorProneWord> page = this.errorProneWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), ErrorProneWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping
	public R<?> add(@RequestBody @Validated ErrorProneWord errorProneWord) {
		errorProneWord.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.errorProneWordService.addErrorProneWord(errorProneWord);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PutMapping
	public R<String> edit(@RequestBody @Validated ErrorProneWord errorProneWord) {
		errorProneWord.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.errorProneWordService.updateErrorProneWord(errorProneWord);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<Long> errorProneWordIds) {
		this.errorProneWordService.removeByIds(errorProneWordIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/check")
	public R<?> check(@RequestBody String text) {
		Map<String, String> map = this.errorProneWordService.check(text);
		return R.ok(map.entrySet().stream().map(e -> Map.of("w", e.getKey(), "r", e.getValue())).toList());
	}
}
