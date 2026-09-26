package com.chestnut.system.utils;

import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.ArrayUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.service.ISysDeptService;

import java.util.Objects;
import java.util.function.Supplier;

public class SysDeptUtils {

    public static final String ANCESTORS_SPLITTER = ",";

    public static final ISysDeptService sysDeptService = SpringUtils.getBean(ISysDeptService.class);

    public static SysDept getDept(Long deptId) {
        return sysDeptService.getDept(deptId);
    }

    /**
     * 检查用户是否拥有指定部门的权限，用户默认用于所属机构及其下级机构权限
     */
    public static void checkDeptScope(LoginUser loginUser, Supplier<Long> targetDeptIdS, Supplier<String> targetDeptAncestorsS) {
        SysUser user = (SysUser) loginUser.getUser();
        checkDeptScope(user, targetDeptIdS, targetDeptAncestorsS);
    }

    public static void checkDeptScope(SysUser user, Supplier<Long> targetDeptIdS, Supplier<String> targetDeptAncestorsS) {
        if (SecurityUtils.isSuperAdmin(user.getUserId())) {
            return;
        }
        boolean canManage = canManageDept(user::getDeptId, targetDeptIdS, targetDeptAncestorsS);
        Assert.isTrue(canManage, SysErrorCode.DEPT_ACCESS_DENY::exception);
    }

    public static boolean canManageDept(Supplier<Long> operatorDeptIdS, Supplier<Long> targetDeptIdS, Supplier<String> targetDeptAncestorsS) {
        Long operatorDeptId = operatorDeptIdS.get();
        if (operatorDeptId.equals(targetDeptIdS.get())) {
            return true;
        }
        String[] ancestorsArr = targetDeptAncestorsS.get().split(ANCESTORS_SPLITTER);
        return ArrayUtils.contains(operatorDeptId.toString(), ancestorsArr);
    }

    public static String getDeptAncestors(String parentAncestors, Long deptId) {
        if (StringUtils.isNotEmpty(parentAncestors)) {
            return parentAncestors + ANCESTORS_SPLITTER + deptId;
        }
        return deptId.toString();
    }

    public static String getDeptAncestors(SysDept parent, Long deptId) {
        return getDeptAncestors(Objects.isNull(parent) ? null : parent.getAncestors(), deptId);
    }
}
