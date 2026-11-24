package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.ArrayUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.member.core.IMemberSubscribeServiceItem;
import com.chestnut.member.domain.MemberSubscribeDefinition;
import com.chestnut.member.domain.MemberSubscribeService;
import com.chestnut.member.domain.dto.SaveMemberSubscribeServiceReq;
import com.chestnut.member.exception.MemberSubscribeErrorCode;
import com.chestnut.member.mapper.MemberSubscribeServiceMapper;
import com.chestnut.member.service.IMemberSubscribeDefinitionService;
import com.chestnut.member.service.IMemberSubscribeServiceService;
import com.chestnut.system.service.impl.LockService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 维护会员订阅服务项，内置项名称由系统提供，已引用的服务项不能删除。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberSubscribeServiceServiceImpl extends ServiceImpl<MemberSubscribeServiceMapper, MemberSubscribeService> implements IMemberSubscribeServiceService {

    private static final String LOCK_KEY = "cc:member:subscribe:service:lock";

    private final IMemberSubscribeDefinitionService subscribeDefService;

    private final LockService lockService;

    private final List<IMemberSubscribeServiceItem> serviceItems;

    @Override
    public void createMemberSubscribeService(SaveMemberSubscribeServiceReq req) {
        this.lockService.lock(LOCK_KEY, () -> {
            Long count = this.lambdaQuery().eq(MemberSubscribeService::getServiceId, req.getServiceId())
                    .count();
            Assert.isTrue(count == 0, MemberSubscribeErrorCode.SERVICE_CONFLICT::exception);

            MemberSubscribeService entity = new MemberSubscribeService();
            entity.setServiceId(req.getServiceId());
            IMemberSubscribeServiceItem builtIn = findBuiltIn(req.getServiceId());
            if (builtIn == null) {
                Assert.isTrue(StringUtils.isNotBlank(req.getName()), MemberSubscribeErrorCode.INVALID_REQUEST::exception);
            }
            entity.setName(builtIn == null ? req.getName().trim() : I18nUtils.get(builtIn.getName()));
            entity.setRemark(req.getRemark());
            entity.createBy(req.getOperator().getUsername());
            this.save(entity);
        });
    }

    @Override
    public void updateMemberSubscribeService(SaveMemberSubscribeServiceReq req) {
        MemberSubscribeService entity = getById(req.getServiceId());
        Assert.notNull(entity, () ->  CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getServiceId()));

        MemberSubscribeService changes = new MemberSubscribeService();
        changes.setServiceId(entity.getServiceId());
        // 内置项只允许修改备注，不能通过直接调用接口覆盖名称。
        if (findBuiltIn(entity.getServiceId()) == null) {
            Assert.isTrue(StringUtils.isNotBlank(req.getName()), MemberSubscribeErrorCode.INVALID_REQUEST::exception);
            changes.setName(req.getName().trim());
        }
        changes.setRemark(req.getRemark());
        changes.updateBy(req.getOperator().getUsername());
        updateById(changes);
    }

    private IMemberSubscribeServiceItem findBuiltIn(String serviceId) {
        return serviceItems.stream().filter(item -> item.getServiceId().equals(serviceId)).findFirst().orElse(null);
    }

    @Override
    public void deleteMemberSubscribeServices(List<String> ids) {
        List<MemberSubscribeService> services = this.listByIds(ids);
        List<String> serviceIds = services.stream().map(MemberSubscribeService::getServiceId).toList();

        List<MemberSubscribeDefinition> list = this.subscribeDefService.list();
        for (MemberSubscribeDefinition def : list) {
            if(ArrayUtils.containsAny(def.getServiceIds(), serviceIds)) {
                throw MemberSubscribeErrorCode.SERVICE_USED.exception();
            }
        }
        this.removeByIds(ids);
    }
}
