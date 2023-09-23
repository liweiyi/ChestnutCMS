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
