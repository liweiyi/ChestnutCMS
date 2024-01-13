package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysWeChatConfig;

import java.util.List;

/**
 * 微信参数配置 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysWeChatConfigService extends IService<SysWeChatConfig> {

	SysWeChatConfig getBackendWeChatConfig();

	void addWeChatConfig(SysWeChatConfig config);

	void editWeChatConfig(SysWeChatConfig config);

	void deleteWeChatConfigs(List<Long> config);

	void changeConfigStatus(Long configId);
}
