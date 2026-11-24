package com.chestnut.member.permission;

/**
 * 会员订阅基础管理权限。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface MemberSubscribePriv {
    String CONFIG_VIEW = "member:subscription:config:view";
    String DEFINITION_ADD = "member:subscription:definition:add";
    String DEFINITION_EDIT = "member:subscription:definition:edit";
    String DEFINITION_REMOVE = "member:subscription:definition:remove";
    String RECORD_VIEW = "member:subscription:view";
    String ADJUST = "member:subscription:adjust";
    String LOG_VIEW = "member:subscription:log:view";
}
