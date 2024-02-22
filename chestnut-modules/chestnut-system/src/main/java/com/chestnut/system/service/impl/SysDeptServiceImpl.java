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
package com.chestnut.system.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysDeptMapper;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.service.ISysDeptService;

import lombok.RequiredArgsConstructor;

/**
 * 部门管理 服务实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

	private final SysUserMapper userMapper;

	private final RedisCache redisCache;

	@Override
	public Optional<SysDept> getDept(Long deptId) {
		SysDept dept = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId);
		if (Objects.isNull(dept)) {
			dept = this.getById(deptId);
			if (Objects.nonNull(dept)) {
				redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId, dept);
			}
		}
		return Optional.ofNullable(dept);
	}

	/**
	 * 构建前端所需要树结构
	 *
	 * @param depts
	 *            部门列表
	 * @return 树结构列表
	 */
	@Override
	public List<SysDept> buildDeptTree(List<SysDept> depts) {
		List<SysDept> returnList = new ArrayList<SysDept>();
		List<Long> tempList = new ArrayList<Long>();
		for (SysDept dept : depts) {
			tempList.add(dept.getDeptId());
		}
		for (SysDept dept : depts) {
			// 如果是顶级节点, 遍历该父节点的所有子节点
			if (!tempList.contains(dept.getParentId())) {
				recursionFn(depts, dept);
				returnList.add(dept);
			}
		}
		if (returnList.isEmpty()) {
			returnList = depts;
		}
		return returnList;
	}

	/**
	 * 构建前端所需要下拉树结构
	 *
	 * @param depts
	 *            部门列表
	 * @return 下拉树结构列表
	 */
	@Override
	public List<TreeNode<Long>> buildDeptTreeSelect(List<SysDept> depts) {
		List<SysDept> deptTrees = buildDeptTree(depts);
		return deptTrees.stream().map(this::buildTreeSelect).collect(Collectors.toList());
	}

	private TreeNode<Long> buildTreeSelect(SysDept dept) {
		TreeNode<Long> ts = new TreeNode<>(dept.getDeptId(), dept.getParentId(), dept.getDeptName(), false);
		ts.setId(dept.getDeptId());
		ts.setLabel(dept.getDeptName());
		List<TreeNode<Long>> children = dept.getChildren().stream().map(this::buildTreeSelect).collect(Collectors.toList());
		ts.setChildren(children);
		return ts;
	}

	/**
	 * 当前父节点下无同名部门
	 *
	 * @param dept
	 * @return
	 */
	private boolean checkDeptNameUnique(SysDept dept) {
		long count = this.count(new LambdaQueryWrapper<SysDept>()
				.eq(SysDept::getParentId, dept.getParentId())
				.eq(SysDept::getDeptName, dept.getDeptName())
				.ne(IdUtils.validate(dept.getDeptId()), SysDept::getDeptId, dept.getDeptId()));
		return count == 0;
	}

	/**
	 * 新增保存部门信息
	 *
	 * @param dept
	 *            部门信息
	 * @return 结果
	 */
	@Override
	public void insertDept(SysDept dept) {
		SysDept parent = this.getById(dept.getParentId());
		// 如果父节点不为正常状态,则不允许新增子节点
		Assert.isTrue(parent.isEnable(), SysErrorCode.DISBALE_DEPT_ADD_CHILD::exception);

		boolean unique = this.checkDeptNameUnique(dept);
		Assert.isTrue(unique, () -> CommonErrorCode.DATA_CONFLICT.exception(dept.getDeptName()));

		dept.setDeptId(IdUtils.getSnowflakeId());
		dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
		dept.setCreateTime(LocalDateTime.now());
		this.save(dept);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + dept.getDeptId());
	}

	/**
	 * 修改保存部门信息
	 *
	 * @param dept
	 *            部门信息
	 * @return 结果
	 */
	@Override
	public void updateDept(SysDept dept) {
		SysDept db = this.getById(dept.getDeptId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(dept.getDeptId()));
		if (EnableOrDisable.isEnable(db.getStatus()) && EnableOrDisable.isDisable(dept.getStatus())
				&& this.lambdaQuery().likeRight(SysDept::getAncestors, db.getAncestors() + "," + db.getDeptId()).count() > 0) {
			throw CommonErrorCode.SYSTEM_ERROR.exception("该部门包含未停用的子部门！");
		}

		boolean unique = this.checkDeptNameUnique(dept);
		Assert.isTrue(unique, () -> CommonErrorCode.DATA_CONFLICT.exception(dept.getDeptName()));

		db.setDeptName(dept.getDeptName());
		db.setOrderNum(dept.getOrderNum());
		db.setLeader(dept.getLeader());
		db.setPhone(dept.getPhone());
		db.setEmail(dept.getEmail());
		db.setStatus(dept.getStatus());
		db.setUpdateTime(LocalDateTime.now());
		db.updateBy(dept.getUpdateBy());
		this.updateById(dept);

		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + dept.getDeptId());
	}

	/**
	 * 删除部门管理信息
	 *
	 * @param deptId
	 *            部门ID
	 * @return 结果
	 */
	@Override
	public void deleteDeptById(Long deptId) {
		boolean hasChildren = this.lambdaQuery().eq(SysDept::getParentId, deptId).count() > 0;
		Assert.isFalse(hasChildren, () -> CommonErrorCode.SYSTEM_ERROR.exception("存在下级部门,不允许删除"));

		boolean hasUser = this.userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId)) > 0;
		Assert.isFalse(hasUser, () -> CommonErrorCode.SYSTEM_ERROR.exception("部门存在用户,不允许删除"));

		this.removeById(deptId);

		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId);
	}

	/**
	 * 递归列表
	 */
	private void recursionFn(List<SysDept> list, SysDept t) {
		// 得到子节点列表
		List<SysDept> childList = getChildList(list, t);
		t.setChildren(childList);
		for (SysDept tChild : childList) {
			if (hasChild(list, tChild)) {
				recursionFn(list, tChild);
			}
		}
	}

	/**
	 * 得到子节点列表
	 */
	private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
		List<SysDept> tlist = new ArrayList<SysDept>();
		Iterator<SysDept> it = list.iterator();
		while (it.hasNext()) {
			SysDept n = (SysDept) it.next();
			if (Objects.nonNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue()) {
				tlist.add(n);
			}
		}
		return tlist;
	}

	/**
	 * 判断是否有子节点
	 */
	private boolean hasChild(List<SysDept> list, SysDept t) {
		return getChildList(list, t).size() > 0;
	}
}
