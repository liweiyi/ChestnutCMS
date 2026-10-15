package com.chestnut.member.core;

import tools.jackson.databind.node.ObjectNode;

/**
 * 会员登录方式
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberLoginType {

    String BEAN_PREFIX = "MemberLoginType_";

    /**
     * 登录方式唯一标识
     */
    String getType();

    /**
     * 登录
     *
     * @return 登录TOKEN
     */
    String login(ObjectNode req, String ipAddress, String userAgent);

    /**
     * 注册
     *
     * @param req 请求body参数
     * @param ip 来源IP
     * @param userAgent 来源UA
     * @return 登录TOKEN
     */
    String register(ObjectNode req, String ip, String userAgent);
}
