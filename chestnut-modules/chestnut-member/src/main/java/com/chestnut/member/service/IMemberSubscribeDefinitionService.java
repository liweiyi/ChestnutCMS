package com.chestnut.member.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.member.core.IMemberSubscribeServiceItem;
import com.chestnut.member.domain.MemberSubscribeDefinition;
import com.chestnut.member.domain.dto.ChangeSubscribeDefinitionStatusReq;
import com.chestnut.member.domain.dto.CreateSubscribeDefinitionReq;
import com.chestnut.member.domain.dto.UpdateSubscribeDefinitionReq;

/**
 * 会员订阅定义及动态服务范围管理接口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberSubscribeDefinitionService
        extends IService<MemberSubscribeDefinition> {

    /**
     * 获取会员订阅服务项
     *
     * @param serviceId 服务ID
     */
    IMemberSubscribeServiceItem getSubscriptionServiceItem(String serviceId);

    /** 创建唯一key的订阅定义并保存服务范围。 */
    void createDefinition(CreateSubscribeDefinitionReq req);

    /** 更新可编辑字段及当前服务范围，定义key保持不变。 */
    void updateDefinition(UpdateSubscribeDefinitionReq req);

    /** 删除未被会员或业务数据引用的定义。 */
    void deleteDefinition(String subscriptionDefinitionId, LoginUser operator);

    /**
     * 获取订阅定义，优先缓存读取
     *
     * @param subscriptionDefinitionId 订阅定义ID
     * @return 订阅定义，不存在时返回空值
     */
    MemberSubscribeDefinition getSubscriptionDefinition(String subscriptionDefinitionId);

    /** 变更订阅定义状态 */
    void changeDefinitionStatus(ChangeSubscribeDefinitionStatusReq req);
}
