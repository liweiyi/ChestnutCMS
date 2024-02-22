/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.system.permission;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;

/**
 * 权限工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class PermissionUtils {

    /**
     * 校验权限
     *
     * @param perm
     * @param loginUser
     * @return
     */
    public static void checkPermission(String perm, LoginUser loginUser) {
        Assert.isTrue(loginUser.hasPermission(perm),
                () -> new NotPermissionException(perm, loginUser.getUserType())
                        .setCode(SaErrorCode.CODE_11051));
    }
}
