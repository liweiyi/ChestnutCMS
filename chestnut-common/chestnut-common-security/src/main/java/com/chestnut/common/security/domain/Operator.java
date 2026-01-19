/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.common.security.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Operator
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class Operator {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门ID
     */
    private Long deptId;

    public static Operator of(LoginUser loginUser) {
        Operator operator = new Operator();
        operator.setUserId(loginUser.getUserId());
        operator.setUsername(loginUser.getUsername());
        operator.setUserType(loginUser.getUserType());
        operator.setDeptId(loginUser.getDeptId());
        return operator;
    }

    public static Operator of(Long userId, String username, String userType, Long deptId) {
        Operator operator = new Operator();
        operator.setUserId(userId);
        operator.setUsername(username);
        operator.setUserType(userType);
        operator.setDeptId(deptId);
        return operator;
    }

    public static Operator defalutOperator() {
        Operator operator = new Operator();
        operator.setUserId(0L);
        operator.setUsername("_cc_system");
        operator.setUserType(null);
        return operator;
    }
}
