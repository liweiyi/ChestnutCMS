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
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.CreateDeptRequest;
import com.chestnut.system.domain.dto.UpdateDeptRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysDeptMapper;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.utils.SysDeptUtils;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 部门管理 服务实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService, CommandLineRunner {

	private final SysUserMapper userMapper;

	private final RedisCache redisCache;

    private final SysRoleMapper roleMapper;

	private final RedissonClient redissonClient;

	@Override
	public SysDept getDept(Long deptId) {
		SysDept dept = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId, SysDept.class);
		if (Objects.isNull(dept)) {
			dept = this.getById(deptId);
			if (Objects.nonNull(dept)) {
				redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + deptId, dept);
			}
		}
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", deptId));
		return dept;
	}

	@Override
	public List<SysDept> getDepartmentsByUserId(Long userId, Consumer<LambdaQueryChainWrapper<SysDept>> consumer) {
		LambdaQueryChainWrapper<SysDept> q = this.lambdaQuery();
		if (!SecurityUtils.isSuperAdmin(userId)) {
			SysUser user = this.userMapper.selectById(userId);
			SysDept dept = this.getDept(user.getDeptId());
			q.and(and -> and.eq(SysDept::getDeptId, user.getDeptId())
					.or().likeRight(SysDept::getAncestors, dept.getAncestors() + SysDeptUtils.ANCESTORS_SPLITTER));
		}
		consumer.accept(q);
        return q.orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getOrderNum).list();
	}

	@Override
	public List<SysDept> buildDeptTree(List<SysDept> depts) {
		List<SysDept> returnList = new ArrayList<>();
		List<Long> tempIds = new ArrayList<>();
		for (SysDept dept : depts) {
			tempIds.add(dept.getDeptId());
		}
		for (SysDept dept : depts) {
			// 如果是顶级节点, 遍历该父节点的所有子节点
			if (!tempIds.contains(dept.getParentId())) {
				recursionFn(depts, dept);
				returnList.add(dept);
			}
		}
		if (returnList.isEmpty()) {
			returnList = depts;
		}
		return returnList;
	}

	private boolean checkDeptNameUnique(Long parentId, String deptName, Long deptId) {
		long count = this.count(new LambdaQueryWrapper<SysDept>()
				.eq(SysDept::getParentId, parentId)
				.eq(SysDept::getDeptName, deptName)
				.ne(IdUtils.validate(deptId), SysDept::getDeptId, deptId));
		return count == 0;
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
	public void insertDept(CreateDeptRequest req) {
		LoginUser operator = req.getOperator();
		SysDept parent = this.getDept(req.getParentId());
		// 如果父节点不为正常状态,则不允许新增子节点
		Assert.isTrue(Objects.nonNull(parent) && parent.isEnable(), SysErrorCode.DISABLE_DEPT_ADD_CHILD::exception);

		SysDeptUtils.checkDeptScope(operator, parent::getDeptId, parent::getAncestors);

		boolean unique = this.checkDeptNameUnique(req.getParentId(), req.getDeptName(), 0L);
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
		dept.setAncestors(SysDeptUtils.getDeptAncestors(parent.getAncestors(), dept.getDeptId()));
		dept.createBy(req.getOperator().getUsername());
		this.save(dept);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DEPT_KEY + dept.getDeptId());
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
	public void updateDept(UpdateDeptRequest req) {
		LoginUser operator = req.getOperator();
		SysDept db = this.getDept(req.getDeptId());
		Assert.isTrue(Objects.nonNull(db), () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", req.getDeptId()));
		SysDeptUtils.checkDeptScope(operator, db::getDeptId, db::getAncestors);

		if (EnableOrDisable.isEnable(db.getStatus()) && EnableOrDisable.isDisable(req.getStatus())) {
			Long childCount = this.lambdaQuery().eq(SysDept::getStatus, EnableOrDisable.ENABLE)
					.likeRight(SysDept::getAncestors, db.getAncestors() + SysDeptUtils.ANCESTORS_SPLITTER).count();
			Assert.isTrue(childCount == 0, SysErrorCode.HAS_ENABLE_CHILD_DEPT::exception);
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
    @Transactional(rollbackFor = Exception.class)
	public void deleteDeptById(Long deptId, LoginUser operator) {
		SysDept dept = this.getDept(deptId);
		SysDeptUtils.checkDeptScope(operator, dept::getDeptId, dept::getAncestors);

		boolean hasRole = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeptId, deptId)) > 0;
		Assert.isFalse(hasRole, SysErrorCode.ORG_DEL_ROLE::exception);
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
        for (SysDept n : list) {
            if (Objects.nonNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue()) {
                tlist.add(n);
            }
        }
		return tlist;
	}

	private boolean hasChild(List<SysDept> list, SysDept t) {
		return !getChildList(list, t).isEmpty();
	}

	@Override
	public SysDept getTopDept() {
		List<SysDept> list = this.lambdaQuery().eq(SysDept::getParentId, 0).list();
		Assert.isTrue(list.size() == 1, SysErrorCode.INVALID_TOP_DEPT::exception);
		return list.get(0);
	}

	@Override
	public void run(String... args) throws Exception {
		RLock lock = redissonClient.getLock(SysConstants.CACHE_SYS_DEPT_KEY + 0);
		try {
			boolean tryLock = lock.tryLock(5, TimeUnit.SECONDS);
			if (!tryLock) {
				return;
			}
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}
}
