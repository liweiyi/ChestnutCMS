/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

/**
 * 系统模块Controller访问权限统一管理类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface SysMenuPriv {
	
	String MonitorCacheList = "monitor:cache:view";

	String MonitorCacheClear = "monitor:cache:clear";
	
	String MonitorServerList = "monitor:server:view";
	
	String MonitorOnlineList = "monitor:online:view";
	
	String MonitorOnlineForceLogout = "monitor:online:forceLogout";
	
	String MonitorLogsView = "monitor:logs:view";
	
	String AsyncTaskList = "monitor:async:view";
	
	String GroovyExec = "tool:groovy:exec";
	
	String SysConfigList = "system:config:view";

	String SysConfigAdd = "system:config:add";

	String SysConfigEdit = "system:config:edit";

	String SysConfigRemove = "system:config:remove";

	String SysDeptList = "system:dept:view";

	String SysDeptAdd = "system:dept:add";

	String SysDeptEdit = "system:dept:edit";

	String SysDeptRemove = "system:dept:remove";

	String SysDictList = "system:dict:view";

	String SysDictAdd = "system:dict:add";

	String SysDictEdit = "system:dict:edit";

	String SysDictRemove = "system:dict:remove";

	String SysI18NDictList = "system:i18ndict:view";

	String SysI18NDictAdd = "system:i18ndict:add";

	String SysI18NDictEdit = "system:i18ndict:edit";

	String SysI18NDictRemove = "system:i18ndict:remove";
	
	String SysMenuList = "system:menu:view";
	
	String SysMenuAdd = "system:menu:add";
	
	String SysMenuEdit = "system:menu:edit";
	
	String SysMenuRemove = "system:menu:remove";
	
	String SysNoticeList = "system:notice:view";
	
	String SysNoticeAdd = "system:notice:add";
	
	String SysNoticeEdit = "system:notice:edit";
	
	String SysNoticeRemove = "system:notice:remove";
	
	String SysPostList = "system:post:view";
	
	String SysPostAdd = "system:post:add";
	
	String SysPostEdit = "system:post:edit";
	
	String SysPostRemove = "system:post:remove";
	
	String SysRoleList = "system:role:view";
	
	String SysRoleAdd = "system:role:add";
	
	String SysRoleEdit = "system:role:edit";
	
	String SysRoleRemove = "system:role:remove";
	
	String SysSecurityList = "system:security:view";
	
	String SysUserList = "system:user:view";
	
	String SysUserAdd = "system:user:add";
	
	String SysUserEdit = "system:user:edit";
	
	String SysUserRemove = "system:user:remove";
	
	String SysUserResetPwd = "system:user:resetPwd";

	String SysWeChatConfigView = "system:wechat:view";

	String SysApiView = "system:api:view";
}
