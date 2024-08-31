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
package com.chestnut.system.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.schedule.IScheduledHandler;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysUserService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台锁定用户定时解锁任务<br/>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + SysUserUnlockJobHandler.JOB_NAME)
public class SysUserUnlockJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "SysUserUnlockJobHandler";
	
	private final ISecurityConfigService securityConfigService;

	private final ISysUserService sysUserService;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		logger.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();

		long pageSize = 500;
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
				.eq(SysUser::getStatus, UserStatus.LOCK)
				.lt(SysUser::getLockEndTime, LocalDateTime.now());
		long total = sysUserService.count(q);
		for (int i = 0; i * pageSize < total; i++) {
			Page<SysUser> page = sysUserService.page(new Page<>(i, pageSize, false), q);
			List<Long> userIds = page.getRecords().stream().map(SysUser::getUserId).toList();
			sysUserService.lambdaUpdate().set(SysUser::getStatus, UserStatus.ENABLE).in(SysUser::getUserId, userIds).update();
		}
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
