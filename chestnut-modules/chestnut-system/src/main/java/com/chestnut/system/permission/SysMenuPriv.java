package com.chestnut.system.permission;

/**
 * 系统模块Controller访问权限统一管理类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface SysMenuPriv {
	
	public String MonitorCacheList = "monitor:cache:view";
	
	public String MonitorServerList = "monitor:server:view";
	
	public String MonitorOnlineList = "monitor:online:view";
	
	public String MonitorOnlineForceLogout = "monitor:online:forceLogout";
	
	public String MonitorLogsView = "monitor:logs:view";
	
	public String AsyncTaskList = "monitor:async:view";
	
	public String GroovyExec = "tool:groovy:exec";
	
	public String SysConfigList = "system:config:view";

	public String SysConfigExport = "system:config:export";

	public String SysConfigAdd = "system:config:add";

	public String SysConfigEdit = "system:config:edit";

	public String SysConfigRemove = "system:config:remove";

	public String SysDeptList = "system:dept:view";

	public String SysDeptAdd = "system:dept:add";

	public String SysDeptEdit = "system:dept:edit";

	public String SysDeptRemove = "system:dept:remove";

	public String SysDictList = "system:dict:view";

	public String SysDictExport = "system:dict:export";

	public String SysDictAdd = "system:dict:add";

	public String SysDictEdit = "system:dict:edit";

	public String SysDictRemove = "system:dict:remove";

	public String SysI18NDictList = "system:i18ndict:view";

	public String SysI18NDictExport = "system:i18ndict:export";

	public String SysI18NDictAdd = "system:i18ndict:add";

	public String SysI18NDictEdit = "system:i18ndict:edit";

	public String SysI18NDictRemove = "system:i18ndict:remove";
	
	public String SysMenuList = "system:menu:view";
	
	public String SysMenuAdd = "system:menu:add";
	
	public String SysMenuEdit = "system:menu:edit";
	
	public String SysMenuRemove = "system:menu:remove";
	
	public String SysNoticeList = "system:notice:view";
	
	public String SysNoticeAdd = "system:notice:add";
	
	public String SysNoticeEdit = "system:notice:edit";
	
	public String SysNoticeRemove = "system:notice:remove";
	
	public String SysPostList = "system:post:view";
	
	public String SysPostExport = "system:post:export";
	
	public String SysPostAdd = "system:post:add";
	
	public String SysPostEdit = "system:post:edit";
	
	public String SysPostRemove = "system:post:remove";
	
	public String SysRoleList = "system:role:view";
	
	public String SysRoleExport = "system:role:export";
	
	public String SysRoleAdd = "system:role:add";
	
	public String SysRoleEdit = "system:role:edit";
	
	public String SysRoleRemove = "system:role:remove";
	
	public String SysSecurityList = "system:security:view";
	
	public String SysUserList = "system:user:view";
	
	public String SysUserExport = "system:user:export";
	
	public String SysUserAdd = "system:user:add";
	
	public String SysUserEdit = "system:user:edit";
	
	public String SysUserRemove = "system:user:remove";
	
	public String SysUserResetPwd = "system:user:resetPwd";
}
