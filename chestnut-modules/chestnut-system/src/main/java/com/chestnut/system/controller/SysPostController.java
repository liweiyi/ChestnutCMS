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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.vo.SysPostSelectVO;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPostService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位信息操作处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/post")
public class SysPostController extends BaseRestController {

	private final ISysPostService postService;

	/**
	 * 获取岗位列表
	 */
	@ExcelExportable(SysPost.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostList)
	@GetMapping("/list")
	public R<?> list(SysPost post) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysPost> q = new LambdaQueryWrapper<SysPost>()
				.like(StringUtils.isNotEmpty(post.getPostName()), SysPost::getPostName, post.getPostName())
				.like(StringUtils.isNotEmpty(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
				.eq(StringUtils.isNotEmpty(post.getStatus()), SysPost::getStatus, post.getStatus())
				.orderByAsc(SysPost::getPostSort);
		Page<SysPost> page = postService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 根据岗位编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostList)
	@GetMapping(value = "/{postId}")
	public R<?> getInfo(@PathVariable @LongId Long postId) {
		return R.ok(postService.getById(postId));
	}

	/**
	 * 新增岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostAdd)
	@Log(title = "岗位管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysPost post) {
		post.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		postService.insertPost(post);
		return R.ok();
	}

	/**
	 * 修改岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostEdit)
	@Log(title = "岗位管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysPost post) {
		post.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		postService.updatePost(post);
		return R.ok();
	}

	/**
	 * 删除岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostRemove)
	@Log(title = "岗位管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> postIds) {
		postService.deletePostByIds(postIds);
		return R.ok();
	}

	/**
	 * 获取岗位选择框列表
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/optionselect")
	public R<?> optionselect() {
		List<SysPostSelectVO> options = postService.list().stream()
				.map(post -> new SysPostSelectVO(post.getPostId(), post.getPostCode(), post.getPostName()))
				.collect(Collectors.toList());
		return R.ok(options);
	}
}
