package com.chestnut.system.security;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import tools.jackson.databind.node.ObjectNode;

/**
 * 后端登录方式
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ILoginType {

    String BEAN_PREFIX = "LoginType_";

    String getType();

    default ObjectNode getLoginConfig(SysSecurityConfig  securityConfig) {
        return JacksonUtils.objectNode();
    }

    String doLogin(ObjectNode requestBody);
}
