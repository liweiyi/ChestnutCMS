package com.chestnut.member.exception;

import com.chestnut.common.exception.ErrorCode;

/**
 * 会员订阅基础服务错误码。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum MemberSubscribeErrorCode implements ErrorCode {
    /**
     * 会员订阅参数无效
     */
    INVALID_REQUEST,

    /**
     * 缺少有效的订阅操作人
     */
    INVALID_OPERATOR,

    /**
     * 订阅定义不存在
     */
    SUBSCRIPTION_NOT_FOUND,

    /**
     * 订阅key已存在
     */
    KEY_EXISTS,

    /**
     * 订阅服务项未注册或参数无效
     */
    SERVICE_ITEM_UNAVAILABLE,

    /**
     * 会员不存在
     */
    MEMBER_NOT_FOUND,

    /**
     * 会员订阅记录不存在
     */
    RECORD_NOT_FOUND,

    /**
     * 订阅记录已变更，请刷新后重试
     */
    VERSION_CONFLICT,

    /**
     * 幂等请求ID已被其他订阅操作使用
     */
    REQUEST_CONFLICT,

    /**
     * 会员订阅定义唯一编码冲突
     */
    CODE_CONFLICT,

    /**
     * 调整秒数或结束时间超出支持范围
     */
    TIME_OVERFLOW,

    /**
     * 订阅定义已被会员或业务数据引用，不能删除
     */
    SUBSCRIPTION_IN_USE,

    /**
     * 不支持的订阅服务：{0}
     */
    UNSUPPORTED_SUBSCRIPTION_SERVICE_ITEM,

    /**
     * 订阅请求重复！！
     */
    REPEAT_SUBSCRIBE,

    /**
     * 订阅服务项重复！
     */
    SERVICE_CONFLICT,

    /**
     * 订阅服务被使用无法删除
     */
    SERVICE_USED,

    /**
     * 会员订阅缺少来源参数
     */
    MissingSource;

    @Override
    public String value() { return "{ERR.MEMBER_SUBSCRIBE." + name() + "}"; }
}
