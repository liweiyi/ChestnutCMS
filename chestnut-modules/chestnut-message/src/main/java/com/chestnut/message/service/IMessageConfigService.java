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
package com.chestnut.message.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.dto.CreateMessageConfigReq;
import com.chestnut.message.domain.dto.TestMessageConfigReq;
import com.chestnut.message.domain.dto.UpdateMessageConfigReq;

import java.util.List;

/**
 * 消息配置服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMessageConfigService extends IService<CcMessageConfig> {

    IMessageType<?> getMessageType(String type);

    CcMessageConfig getMessageConfig(Long configId);

	void addConfig(CreateMessageConfigReq config);

	void updateConfig(UpdateMessageConfigReq config);

	void deleteConfigs(List<Long> configIds);

    void testSend(TestMessageConfigReq req);
}
