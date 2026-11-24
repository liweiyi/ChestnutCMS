package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.core.IMemberSubscribeServiceItem;
import com.chestnut.member.domain.MemberSubscribeDefinition;
import com.chestnut.member.domain.MemberSubscribeLog;
import com.chestnut.member.domain.MemberSubscribeService;
import com.chestnut.member.domain.dto.ChangeSubscribeDefinitionStatusReq;
import com.chestnut.member.domain.dto.CreateSubscribeDefinitionReq;
import com.chestnut.member.domain.dto.UpdateSubscribeDefinitionReq;
import com.chestnut.member.exception.MemberSubscribeErrorCode;
import com.chestnut.member.mapper.MemberSubscribeDefinitionMapper;
import com.chestnut.member.mapper.MemberSubscribeLogMapper;
import com.chestnut.member.mapper.MemberSubscribeServiceMapper;
import com.chestnut.member.service.IMemberSubscribeDefinitionService;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.service.impl.LockService;
import com.chestnut.system.utils.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 会员订阅定义与动态服务范围管理。 配置读写共享分布式读写锁，编辑只更新允许修改的字段，统计使用数据库原子增量。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberSubscribeDefinitionServiceImpl
        extends ServiceImpl<MemberSubscribeDefinitionMapper, MemberSubscribeDefinition>
        implements IMemberSubscribeDefinitionService {

    private static final String LOCK_PREFIX = "cc:member:subscribe_defi:lock:";
    /** 定义对象使用独立命名空间，避免迁移后读取旧包名的序列化缓存。 */
    private static final String CACHE_PREFIX = "cc:member:subscribe:definition:";

    private final MemberSubscribeLogMapper memberSubscribeLogMapper;
    private final MemberSubscribeServiceMapper memberSubscribeServiceMapper;
    private final LockService lockService;
    private final RedisCache redisCache;
    private final TransactionTemplate transactionTemplate;
    private final Map<String, IMemberSubscribeServiceItem> subscriptionServiceItemMap;

    @Override
    public IMemberSubscribeServiceItem getSubscriptionServiceItem(String serviceId) {
        IMemberSubscribeServiceItem item = subscriptionServiceItemMap.get(IMemberSubscribeServiceItem.BEAN_PREFIX + serviceId);
        Assert.notNull(item, () -> MemberSubscribeErrorCode.UNSUPPORTED_SUBSCRIPTION_SERVICE_ITEM.exception(serviceId));
        return item;
    }

    @Override
    public MemberSubscribeDefinition getSubscriptionDefinition(String id) {
        return redisCache.getCacheObject(CACHE_PREFIX + id, MemberSubscribeDefinition.class, () -> {
            return TransactionUtils.execIndependentForReadOnly(transactionTemplate, status -> getById(id));
        });
    }

    private void validateServiceItems(List<String> ids) {
        if (ids.isEmpty()) {
            return;
        }
        List<String> available = new LambdaQueryChainWrapper<>(memberSubscribeServiceMapper)
                .select(MemberSubscribeService::getServiceId)
                .in(MemberSubscribeService::getServiceId, ids)
                .list().stream().map(MemberSubscribeService::getServiceId).toList();
        for (String id : ids) {
            Assert.isTrue(id != null && available.contains(id),
                    () -> MemberSubscribeErrorCode.UNSUPPORTED_SUBSCRIPTION_SERVICE_ITEM.exception(id));
        }
    }

    @Override
    public void createDefinition(CreateSubscribeDefinitionReq req) {
        validateServiceItems(req.getServiceIds());

        MemberSubscribeDefinition definition = new MemberSubscribeDefinition();
        definition.setDefinitionId(IdUtils.simpleUUID());
        definition.setSubscriptionName(req.getSubscriptionName());
        definition.setDescription(Objects.requireNonNullElse(req.getDescription(), ""));
        definition.setServiceIds(req.getServiceIds().stream().distinct().toList());
        definition.setStatus(EnableOrDisable.ENABLE);
        definition.setRemark(Objects.requireNonNullElse(req.getRemark(), ""));
        definition.createBy(req.getOperator().getUsername());
        this.save(definition);
    }

    @Override
    public void updateDefinition(UpdateSubscribeDefinitionReq req) {
        validateServiceItems(req.getServiceIds());

        this.lockService.lock(LOCK_PREFIX + req.getSubscriptionId(), () -> {
            MemberSubscribeDefinition db = this.getById(req.getSubscriptionId());
            Assert.notNull(db, MemberSubscribeErrorCode.SUBSCRIPTION_NOT_FOUND::exception);

            MemberSubscribeDefinition changes = new MemberSubscribeDefinition();
            changes.setDefinitionId(req.getSubscriptionId());
            changes.setSubscriptionName(req.getSubscriptionName());
            changes.setDescription(Objects.requireNonNullElse(req.getDescription(), ""));
            changes.setServiceIds(req.getServiceIds().stream().distinct().toList());
            changes.setRemark(Objects.requireNonNullElse(req.getRemark(), ""));
            changes.updateBy(req.getOperator().getUsername());
            updateById(changes);
            // 删除缓存
            redisCache.deleteObject(CACHE_PREFIX + req.getSubscriptionId());
        });
    }

    @Override
    public void changeDefinitionStatus(ChangeSubscribeDefinitionStatusReq req) {
        lockService.lock(LOCK_PREFIX + req.getSubscriptionId(), () -> {
            MemberSubscribeDefinition db = getById(req.getSubscriptionId());
            Assert.notNull(db, MemberSubscribeErrorCode.SUBSCRIPTION_NOT_FOUND::exception);

            lambdaUpdate().set(MemberSubscribeDefinition::getStatus, req.getStatus())
                    .set(MemberSubscribeDefinition::getUpdateBy, req.getOperator().getUsername())
                    .set(MemberSubscribeDefinition::getUpdateTime, LocalDateTime.now())
                    .eq(MemberSubscribeDefinition::getDefinitionId, req.getSubscriptionId())
                    .update();
            // 清理缓存。
            redisCache.deleteObject(CACHE_PREFIX + req.getSubscriptionId());
        });
    }

    @Override
    public void deleteDefinition(String id, LoginUser operator) {
        MemberSubscribeDefinition db = getById(id);
        Assert.notNull(db, MemberSubscribeErrorCode.SUBSCRIPTION_NOT_FOUND::exception);
        // 只要有过订阅记录的都不能删除
        Page<MemberSubscribeLog> logs = new LambdaQueryChainWrapper<>(memberSubscribeLogMapper)
                .select(MemberSubscribeLog::getLogId)
                .eq(MemberSubscribeLog::getSubscriptionId, id)
                .page(new Page<>(1, 1, false));
        Assert.isTrue(logs.getRecords().isEmpty(), MemberSubscribeErrorCode.SUBSCRIPTION_IN_USE::exception);

        removeById(id);
        redisCache.deleteObject(CACHE_PREFIX + id);
    }
}
