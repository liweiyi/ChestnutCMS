package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.member.domain.Member;
import com.chestnut.member.MemberSubscribeUtils;
import com.chestnut.member.domain.MemberSubscribeDefinition;
import com.chestnut.member.domain.MemberSubscribeLog;
import com.chestnut.member.domain.MemberSubscribeRecord;
import com.chestnut.member.domain.dto.AdjustSubscribeDurationReq;
import com.chestnut.member.exception.MemberSubscribeErrorCode;
import com.chestnut.member.mapper.MemberSubscribeLogMapper;
import com.chestnut.member.mapper.MemberSubscribeRecordMapper;
import com.chestnut.member.service.IMemberSubscribeDefinitionService;
import com.chestnut.member.service.IMemberSubscribeRecordService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.service.impl.LockService;
import com.chestnut.system.service.impl.LockService.LockOptions;
import com.chestnut.system.utils.TransactionUtils;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会员订阅结束时间、服务权限及调整审计的应用服务。
 * 会员锁保护会员范围内的幂等调整和完整缓存回填，独立读写事务在锁内完成。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberSubscribeRecordServiceImpl
        extends ServiceImpl<MemberSubscribeRecordMapper, MemberSubscribeRecord>
        implements IMemberSubscribeRecordService {

    private static final String LOCK_PREFIX = "cc:member:subscribe:lock:";
    /** 秒时间戳缓存与旧毫秒缓存隔离。 */
    private static final String CACHE_PREFIX = "cc:member:subscribe:record:seconds:";
    private static final LockOptions LOCK_OPTIONS = LockOptions.waitFor(10, TimeUnit.SECONDS);

    private final IMemberSubscribeDefinitionService definitionService;
    private final MemberSubscribeLogMapper logMapper;
    private final IMemberService memberService;
    private final LockService lockService;
    private final RedisCache redisCache;
    private final TransactionTemplate transactionTemplate;
    private final Validator validator;

    @Override
    public MemberSubscribeLog adjustDuration(AdjustSubscribeDurationReq req, Operator operator) {
        // 校验参数
        validate(req, operator);
        return lockService.lockWithResult(LOCK_PREFIX + req.getMemberId(), LOCK_OPTIONS,() -> {
            // 独立事务处理
            MemberSubscribeLog receipt = TransactionUtils.execIndependent(transactionTemplate, status -> {
                // 同一个会员用户相同来源只能触发一次
                Optional<MemberSubscribeLog> existing = new LambdaQueryChainWrapper<>(logMapper)
                    .eq(MemberSubscribeLog::getMemberId, req.getMemberId())
                    .eq(MemberSubscribeLog::getSourceType, req.getSourceType())
                    .eq(MemberSubscribeLog::getSourceId, req.getSourceId())
                    .oneOpt();
                if (existing.isPresent()) {
                    // 同源重试也清理会员缓存，补偿首次事务提交后缓存删除失败。
                    redisCache.deleteObject(CACHE_PREFIX + req.getMemberId());
                    return existing.get();
                }
                MemberSubscribeDefinition definition = definitionService.getById(req.getSubscriptionId());
                Assert.notNull(definition, MemberSubscribeErrorCode.SUBSCRIPTION_NOT_FOUND::exception);
                Member member = memberService.getById(req.getMemberId());
                Assert.notNull(member, MemberSubscribeErrorCode.MEMBER_NOT_FOUND::exception);

                Instant now = Instant.now();
                Optional<MemberSubscribeRecord> opt = this.lambdaQuery()
                        .eq(MemberSubscribeRecord::getMemberId, req.getMemberId())
                        .eq(MemberSubscribeRecord::getSubscriptionId, req.getSubscriptionId())
                        .oneOpt();
                boolean firstAssociation = opt.isEmpty();
                MemberSubscribeRecord _record = opt.orElseGet(() -> {
                    MemberSubscribeRecord newRecord = new MemberSubscribeRecord();
                    newRecord.setRecordId(IdUtils.simpleUUID());
                    newRecord.setMemberId(req.getMemberId());
                    newRecord.setSubscriptionId(req.getSubscriptionId());
                    newRecord.setExpireTime(0L);
                    newRecord.setCreateTime(now.toEpochMilli());
                    return newRecord;
                });
                long beforeExpireTime = _record.getExpireTime();
                Instant subscribeExpireTime = MemberSubscribeUtils.getSubscribeExpireTime(Instant.ofEpochSecond(beforeExpireTime), now, req.getDeltaSeconds());
                _record.setExpireTime(subscribeExpireTime.getEpochSecond());
                if (firstAssociation) {
                    save(_record);
                } else {
                    updateById(_record);
                }
                // 记录审计日志
                MemberSubscribeLog _log = new MemberSubscribeLog();
                _log.setLogId(java.util.UUID.randomUUID().toString().replace("-", ""));
                _log.setRecordId(_record.getRecordId());
                _log.setMemberId(req.getMemberId());
                _log.setSubscriptionId(req.getSubscriptionId());
                _log.setDeltaSeconds(req.getDeltaSeconds());
                _log.setBeforeExpireTime(beforeExpireTime);
                _log.setAfterExpireTime(_record.getExpireTime());
                _log.setOperationTime(now.toEpochMilli());
                _log.setOperatorType(operator.getUserType());
                _log.setOperatorId(operator.getUserId().toString());
                _log.setSourceType(req.getSourceType());
                _log.setSourceId(req.getSourceId());
                _log.setReason(req.getReason());
                logMapper.insert(_log);
                // 事务提交后更新缓存
                TransactionUtils.afterCommit(() -> {
                    redisCache.deleteObject(CACHE_PREFIX + req.getMemberId());
                });
                return _log;
            });
            return receipt;
        });
    }

    private void validate(AdjustSubscribeDurationReq req, Operator operator) {
        Assert.isTrue(req != null && validator.validate(req).isEmpty(),
                MemberSubscribeErrorCode.INVALID_REQUEST::exception);
        Assert.isTrue(operator != null && operator.getUserId() != null && operator.getUserType() != null,
                MemberSubscribeErrorCode.INVALID_OPERATOR::exception);
        ValidationUtils.notBlank(req.getSourceType(), MemberSubscribeErrorCode.MissingSource::exception);
        ValidationUtils.notBlank(req.getSourceId(), MemberSubscribeErrorCode.MissingSource::exception);
    }

    @Override
    public boolean isSubscribedService(Long memberId, String serviceItemId) {
        List<MemberSubscribe> subscribes = getMemberSubscribes(memberId);
        long now = Instant.now().getEpochSecond();
        return subscribes.stream()
                .anyMatch(record -> {
                    if (record.expireTime() <= now) {
                        return false;
                    }
                    MemberSubscribeDefinition definition = definitionService.getSubscriptionDefinition(record.subscribeId());
                    return definition != null && EnableOrDisable.ENABLE.equals(definition.getStatus())
                            && definition.getServiceIds().contains(serviceItemId);
                });
    }

    @Override
    public List<MemberSubscribe> getMemberSubscribes(Long memberId) {
        Member member = memberService.getById(memberId);
        Assert.notNull(member, MemberSubscribeErrorCode.MEMBER_NOT_FOUND::exception);
        return lockService.lockWithResult(LOCK_PREFIX + memberId, LOCK_OPTIONS, () -> {
            Map<String, Long> values = redisCache.getCacheMap(CACHE_PREFIX + memberId, Long.class, 300, TimeUnit.SECONDS, () ->
                TransactionUtils.execIndependentForReadOnly(transactionTemplate, status ->
                    lambdaQuery().select(MemberSubscribeRecord::getSubscriptionId, MemberSubscribeRecord::getExpireTime)
                        .eq(MemberSubscribeRecord::getMemberId, memberId)
                        .list()
                        .stream()
                        .collect(Collectors.toMap(MemberSubscribeRecord::getSubscriptionId, MemberSubscribeRecord::getExpireTime))
                )
            );
            return values.entrySet().stream()
                .map(entry -> new MemberSubscribe(entry.getKey(), entry.getValue()))
                .toList();
        });
    }

    /**
     * 会员订阅定义与结束时间的只读缓存视图，结束时间为UTC纪元秒。
     *
     * @author 兮玥
     * @email 190785909@qq.com
     */
    public record MemberSubscribe(String subscribeId, Long expireTime) {}
}
