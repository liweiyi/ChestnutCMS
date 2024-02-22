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

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.validator.LongId;

import lombok.RequiredArgsConstructor;

/**
 * 部门信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController extends BaseRestController {

	private final ISysDeptService deptService;
	
	/**
	 * 获取部门列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptList)
	@GetMapping("/list")
	public R<?> list(SysDept dept) {
		LambdaQueryWrapper<SysDept> q = new LambdaQueryWrapper<SysDept>()
				.like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
				.eq(StringUtils.isNotEmpty(dept.getStatus()), SysDept::getStatus, dept.getStatus())
				.orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getOrderNum);
		List<SysDept> list = deptService.list(q);
		return bindDataTable(list);
	}

	/**
	 * 根据部门编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptList)
	@GetMapping(value = "/{deptId}")
	public R<?> getInfo(@PathVariable @LongId Long deptId) {
		SysDept dept = deptService.getById(deptId);
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(deptId));
		this.deptService.getDept(dept.getParentId()).ifPresent(d -> dept.setParentName(d.getDeptName()));
		return R.ok(dept);
	}

	/**
	 * 新增部门
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptAdd)
	@Log(title = "部门管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDept dept) {
		dept.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		deptService.insertDept(dept);
		return R.ok();
	}

	/**
	 * 修改部门
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptEdit)
	@Log(title = "部门管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDept dept) {
		dept.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		deptService.updateDept(dept);
		return R.ok();
	}

	/**
	 * 删除部门
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptRemove)
	@Log(title = "部门管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deptId}")
	public R<?> remove(@PathVariable @LongId Long deptId) {
		deptService.deleteDeptById(deptId);
		return R.ok();
	}
}
