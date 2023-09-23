package com.chestnut.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.security.ISecurityUser;

public interface ISecurityConfigService extends IService<SysSecurityConfig> {
	
	/**
	 * 获取安全配置信息
	 * 
	 * @return
	 */
	SysSecurityConfig getSecurityConfig();
	
	/**
	 * 添加安全配置信息
	 * 
	 * @param config
	 * @return
	 */
	void addConfig(SysSecurityConfig config);

	/**
	 * 保存安全配置信息
	 * 
	 * @param config
	 * @return
	 */
	void saveConfig(SysSecurityConfig config);
	
	/**
	 * 删除安全配置信息
	 * 
	 * @param configIds
	 * @param operator
	 */
	void deleteConfigs(List<Long> configIds);
	
	/**
	 * 密码错误处理，如果触发了安全策略返回false。
	 * 
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean processLoginPasswordError(ISecurityUser user);

	/**
	 * 登录成功处理
	 * 
	 * @param user
	 */
	void onLoginSuccess(ISecurityUser user);

	/**
	 * 校验密码
	 *
	 * @param user
	 * @param password
	 * @return
	 */
	void validPassword(ISecurityUser user, String password);

	/**
	 * 重置密码后强制下次登录修改密码
	 * 
	 * @param user
	 */
	void forceModifyPwdAfterResetPwd(ISecurityUser user);

	/**
	 * 后台添加 用户后强制下次登录修改密码
	 * 
	 * @param user
	 */
	void forceModifyPwdAfterUserAdd(ISecurityUser user);

	/**
	 * 启用指定安全配置
	 * 
	 * @param configId
	 */
	void changeConfigStatus(Long configId);
}
