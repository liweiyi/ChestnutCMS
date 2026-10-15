package com.chestnut.system.security.config;

import jakarta.annotation.Nonnull;
import tools.jackson.databind.node.ObjectNode;

/**
 * 登录安全配置校验器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISecurityConfigValidator {

    void validate(@Nonnull ObjectNode configs);
}
