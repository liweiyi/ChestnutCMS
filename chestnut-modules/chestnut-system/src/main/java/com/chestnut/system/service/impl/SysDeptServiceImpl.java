/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
import com.chestnut.system.domain.dto.CreateDeptRequest;
import com.chestnut.system.domain.dto.UpdateDeptRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysDeptMapper;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
		SysDept dept = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId, SysDept.class);
		if (Objects.isNull(dept)) {
			dept = this.getById(deptId);
			if (Objects.nonNull(dept)) {
				redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId, dept);
			}
		}
		return Optional.ofNullable(dept);
	}

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

	private boolean checkDeptNameUnique(Long parentId, String deptName, Long deptId) {
		long count = this.count(new LambdaQueryWrapper<SysDept>()
				.eq(SysDept::getParentId, parentId)
				.eq(SysDept::getDeptName, deptName)
				.ne(IdUtils.validate(deptId), SysDept::getDeptId, deptId));
		return count == 0;
	}

	@Override
	public void insertDept(CreateDeptRequest req) {
		SysDept parent = this.getById(req.getParentId());
		// 如果父节点不为正常状态,则不允许新增子节点
		Assert.isTrue(parent.isEnable(), SysErrorCode.DISABLE_DEPT_ADD_CHILD::exception);

		boolean unique = this.checkDeptNameUnique(req.getParentId(),req.getDeptName(), 0L);
		Assert.isTrue(unique, () -> CommonErrorCode.DATA_CONFLICT.exception(req.getDeptName()));

		SysDept dept = new SysDept();
		dept.setParentId(req.getParentId());
		dept.setDeptName(req.getDeptName());
		dept.setOrderNum(req.getOrderNum());
		dept.setLeader(req.getLeader());
		dept.setPhone(req.getPhone());
		dept.setEmail(req.getEmail());
		dept.setStatus(req.getStatus());
		dept.setDeptId(IdUtils.getSnowflakeId());
		dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
		dept.createBy(req.getOperator().getUsername());
		this.save(dept);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + dept.getDeptId());
	}

	@Override
	public void updateDept(UpdateDeptRequest req) {
		SysDept db = this.getById(req.getDeptId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getDeptId()));
		if (EnableOrDisable.isEnable(db.getStatus()) && EnableOrDisable.isDisable(req.getStatus())
				&& this.lambdaQuery().likeRight(SysDept::getAncestors, db.getAncestors() + "," + db.getDeptId()).count() > 0) {
			throw SysErrorCode.HAS_ENABLE_CHILD_DEPT.exception();
		}

		boolean unique = this.checkDeptNameUnique(req.getParentId(), req.getDeptName(), req.getDeptId());
		Assert.isTrue(unique, () -> CommonErrorCode.DATA_CONFLICT.exception(req.getDeptName()));

		db.setDeptName(req.getDeptName());
		db.setOrderNum(req.getOrderNum());
		db.setLeader(req.getLeader());
		db.setPhone(req.getPhone());
		db.setEmail(req.getEmail());
		db.setStatus(req.getStatus());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);

		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + db.getDeptId());
	}

	@Override
	public void deleteDeptById(Long deptId) {
		boolean hasChildren = this.lambdaQuery().eq(SysDept::getParentId, deptId).count() > 0;
		Assert.isFalse(hasChildren, SysErrorCode.DEPT_DEL_FAIL_HAS_CHILD::exception);

		boolean hasUser = this.userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId)) > 0;
		Assert.isFalse(hasUser, SysErrorCode.DEPT_DEL_FAIL_HAS_USER::exception);

		this.removeById(deptId);

		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId);
	}

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

	private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
		List<SysDept> tlist = new ArrayList<>();
		Iterator<SysDept> it = list.iterator();
		while (it.hasNext()) {
			SysDept n = it.next();
			if (Objects.nonNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue()) {
				tlist.add(n);
			}
		}
		return tlist;
	}

	private boolean hasChild(List<SysDept> list, SysDept t) {
		return !getChildList(list, t).isEmpty();
	}
}
