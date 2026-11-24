package com.chestnut.member.core;

/**
 * 业务模块注册的会员订阅服务项。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberSubscribeServiceItem {

    String BEAN_PREFIX = "MSubscribeServiceItem_";

    /**
     * 返回全局唯一且稳定的服务项ID。
     */
    String getServiceId();

    /**
     * 返回服务名称的国际化键。
     */
    String getName();

    /**
     * 返回服务说明的国际化键。
     */
    String getDescription();
}
