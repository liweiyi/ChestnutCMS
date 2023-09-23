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
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.ISensitiveWordService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 敏感词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/sensitiveword")
public class SensitiveWordController extends BaseRestController {

	private final ISensitiveWordService sensitiveWordService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SensitiveWord> page = this.sensitiveWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SensitiveWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping
	public R<?> add(@RequestBody @Validated SensitiveWord sensitiveWord) {
		sensitiveWord.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.sensitiveWordService.addWord(sensitiveWord);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PutMapping
	public R<?> edit(@RequestBody @Validated SensitiveWord sensitiveWord) {
		Assert.isTrue(IdUtils.validate(sensitiveWord.getWordId()),
				() -> CommonErrorCode.INVALID_REQUEST_ARG.exception("wordId"));

		sensitiveWord.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.sensitiveWordService.editWord(sensitiveWord);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> sensitiveWordIds) {
		this.sensitiveWordService.deleteWord(sensitiveWordIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/check")
	public R<?> check(@RequestBody String text) {
		Set<String> words = this.sensitiveWordService.check(text);
		return R.ok(words);
	}
}
