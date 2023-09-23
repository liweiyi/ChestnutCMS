package com.chestnut.common.extend.enums;

/**
 * 限流类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum LimitType
{
    /**
     * 默认策略全局限流
     */
    DEFAULT,

    /**
     * 根据请求者IP进行限流
     */
    IP
}
