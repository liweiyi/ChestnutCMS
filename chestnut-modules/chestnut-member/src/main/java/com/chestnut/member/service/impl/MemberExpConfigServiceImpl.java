/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.cache.MemberExpOpMonitoredCache;
import com.chestnut.member.domain.MemberExpConfig;
import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.domain.MemberLevelExpLog;
import com.chestnut.member.domain.dto.CreateMemberExpConfigRequest;
import com.chestnut.member.domain.dto.UpdateMemberExpConfigRequest;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.level.IExpOperation;
import com.chestnut.member.level.LevelManager;
import com.chestnut.member.mapper.MemberExpConfigMapper;
import com.chestnut.member.service.IMemberExpConfigService;
import com.chestnut.member.service.IMemberLevelConfigService;
import com.chestnut.member.service.IMemberLevelExpLogService;
import com.chestnut.member.service.IMemberLevelService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MemberExpConfigServiceImpl extends ServiceImpl<MemberExpConfigMapper, MemberExpConfig>
		implements IMemberExpConfigService {

	private final MemberExpOpMonitoredCache expOpCache;

	private final Map<String, IExpOperation> expOperations;

	private final IMemberLevelService memberLevelService;

	private final IMemberLevelConfigService memberLevelConfigService;

	private final IMemberLevelExpLogService expLogService;

	private final RedissonClient redissonClient;

	@Override
	public IExpOperation getExpOperation(String opType) {
		IExpOperation eo = this.expOperations.get(IExpOperation.BEAN_PREFIX + opType);
		Assert.notNull(eo, () -> MemberErrorCode.UNSUPPORTED_EXP_OP.exception(opType));
		return eo;
	}

	@Override
	public Map<String, IExpOperation> getExpOperations() {
		return this.expOperations;
	}

	@Override
	public MemberExpConfig getMemberExpOperation(String opType, String levelType) {
		return this.expOpCache.get(opType, levelType,
				() -> this.lambdaQuery().eq(MemberExpConfig::getOpType, opType)
                        .eq(MemberExpConfig::getLevelType, levelType).one());
	}

	@Override
	public void addExpOperation(CreateMemberExpConfigRequest req) {
		IExpOperation expOperation = this.getExpOperation(req.getOpType());

		Long count = this.lambdaQuery().eq(MemberExpConfig::getOpType, expOperation.getId())
				.eq(MemberExpConfig::getLevelType, req.getLevelType()).count();
		Assert.isTrue(count == 0,
				() -> MemberErrorCode.EXP_CONFIG_EXIST.exception(req.getOpType() + "=" + req.getLevelType()));

		MemberExpConfig config = new MemberExpConfig();
		BeanUtils.copyProperties(req, config);
		config.setConfigId(IdUtils.getSnowflakeId());
		config.createBy(req.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void updateExpOperation(UpdateMemberExpConfigRequest req) {
		MemberExpConfig db = this.getById(req.getConfigId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", req.getConfigId()));

		db.setExp(req.getExp());
		db.setDayLimit(req.getDayLimit());
		db.setTotalLimit(req.getTotalLimit());
		db.setRemark(req.getRemark());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
		this.expOpCache.clear(db);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void deleteExpOperations(List<Long> expOperationIds) {
		List<MemberExpConfig> memberExpConfigs = this.listByIds(expOperationIds);
		this.removeBatchByIds(memberExpConfigs);
		expOpCache.clearAll(memberExpConfigs);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void triggerExpOperation(String expOpType, Long memberId) {
		// 获取用户锁，避免并发导致经验值错误
		RLock lock = this.redissonClient.getLock("TriggerExpOp_" + memberId);
		lock.lock();
		try {
			List<MemberExpConfig> list = this.lambdaQuery().eq(MemberExpConfig::getOpType, expOpType).list();
			for (MemberExpConfig memberExpOperation : list) {
				String levelType = memberExpOperation.getLevelType();
				MemberLevel memberLevel = this.memberLevelService.getMemberLevel(memberId, levelType);
				// 校验上限
				if (!this.checkLimit(memberExpOperation, memberId)) {
					continue;
				}
				// 执行通用经验值变更处理逻辑
				LevelManager levelManager = memberLevelConfigService.getLevelManager(memberExpOperation.getLevelType());
				levelManager.addExp(memberLevel, memberExpOperation.getExp());
				this.memberLevelService.updateById(memberLevel);
				// 记录经验值变更日志
				MemberLevelExpLog expLog = new MemberLevelExpLog();
				expLog.setLogId(IdUtils.getSnowflakeId());
				expLog.setLogTime(LocalDateTime.now());
				expLog.setLevelType(levelType);
				expLog.setMemberId(memberId);
				expLog.setOpType(expOpType);
				expLog.setChangeExp(memberExpOperation.getExp());
				expLog.setLevel(memberLevel.getLevel());
				expLog.setExp(memberLevel.getExp());
				this.expLogService.save(expLog);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 校验日/总次数上限
	 * 
	 * TODO: 数据量可以独立存储操作次数记录进行缓存优化
	 */
	private boolean checkLimit(MemberExpConfig memberExpOperation, Long memberId) {
		if (memberExpOperation.getDayLimit() > 0) {
			Long dayCount = this.expLogService.lambdaQuery().eq(MemberLevelExpLog::getMemberId, memberId)
					.eq(MemberLevelExpLog::getOpType, memberExpOperation.getOpType())
					.eq(MemberLevelExpLog::getLevelType, memberExpOperation.getLevelType())
					.ge(MemberLevelExpLog::getLogTime, DateUtils.getTodayStart()).count();
			if (dayCount >= memberExpOperation.getDayLimit()) {
				return false;
			}
		}
		if (memberExpOperation.getTotalLimit() > 0) {
			Long total = this.expLogService.lambdaQuery().eq(MemberLevelExpLog::getMemberId, memberId)
					.eq(MemberLevelExpLog::getOpType, memberExpOperation.getOpType())
					.eq(MemberLevelExpLog::getLevelType, memberExpOperation.getLevelType()).count();
			if (total >= memberExpOperation.getTotalLimit()) {
				return false;
			}
		}
		return true;
	}
}
