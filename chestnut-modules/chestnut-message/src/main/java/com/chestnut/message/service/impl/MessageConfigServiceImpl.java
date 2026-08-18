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
package com.chestnut.message.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.core.email.EmailProps;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.dto.CreateMessageConfigReq;
import com.chestnut.message.domain.dto.TestMessageConfigReq;
import com.chestnut.message.domain.dto.UpdateMessageConfigReq;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.listener.event.AfterMessagePusherConfigUpdateEvent;
import com.chestnut.message.mapper.CcMessageConfigMapper;
import com.chestnut.message.service.IMessageConfigService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageConfigServiceImpl extends ServiceImpl<CcMessageConfigMapper, CcMessageConfig>
		implements IMessageConfigService, ApplicationContextAware {

	private static final String CACHE_PREFIX = "cc:message:config:";

	private ApplicationContext applicationContext;

	private final RedisCache redisCache;

    private final Map<String, IMessageType<?>> messageTypeMap;

    @Override
    public IMessageType<?> getMessageType(String type) {
        IMessageType<?> mt = messageTypeMap.get(IMessageType.BEAN_PREFIX + type);
        Assert.notNull(mt, () -> MessageErrorCode.UNSUPPORTED_MESSAGE_TYPE.exception(type));
        return mt;
    }

	@Override
	public CcMessageConfig getMessageConfig(Long configId) {
		return this.redisCache.getCacheObject(CACHE_PREFIX + configId, CcMessageConfig.class,
				() -> getById(configId));
	}

	@Override
	public void addConfig(CreateMessageConfigReq req) {
		IMessageType<?> messageType = this.getMessageType(req.getType());
		messageType.validate(req.getConfigProps());

		CcMessageConfig config = new CcMessageConfig();
        BeanUtils.copyProperties(req, config);
        config.setConfigId(IdUtils.getSnowflakeId());
        config.createBy(req.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void updateConfig(UpdateMessageConfigReq req) {
		CcMessageConfig dbConfig = this.getById(req.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", req.getConfigId()));

		IMessageType<?> messageType = this.getMessageType(req.getType());
		messageType.validate(req.getConfigProps());
		EmailProps originalEmailProps = this.getEmailProps(dbConfig);

        BeanUtils.copyProperties(req, dbConfig);
        dbConfig.updateBy(req.getOperator().getUsername());
		this.updateById(dbConfig);
		this.clearEmailSender(originalEmailProps);
		this.clearCache(dbConfig);
	}

	@Override
	public void deleteConfigs(List<Long> configIds) {
		List<CcMessageConfig> configs = this.listByIds(configIds);
		this.removeByIds(configIds);
		configs.stream().map(this::getEmailProps).forEach(this::clearEmailSender);
		configs.forEach(this::clearCache);
	}

    @Override
    public void testSend(TestMessageConfigReq req) {
        CcMessageConfig messageConfig = this.getMessageConfig(req.getConfigId());
        IMessageType<?> messageType = this.getMessageType(messageConfig.getType());
        String title = req.getParams().has("title") ? req.getParams().get("title").asString() : "";
        String content = req.getParams().has("content") ? req.getParams().get("content").asString() : "";
        IMessageType.Message msg = new IMessageType.Message(title, content, req.getParams());
        messageType.send(messageConfig.getConfigProps(), msg);
    }

    private void clearCache(CcMessageConfig config) {
		this.redisCache.deleteObject(CACHE_PREFIX + config.getConfigId());
		this.applicationContext.publishEvent(new AfterMessagePusherConfigUpdateEvent(this, config));
    }

	private EmailProps getEmailProps(CcMessageConfig config) {
		if (!EmailMessageType.TYPE.equals(config.getType())) {
			return null;
		}
		return new EmailProps().fromJson(config.getConfigProps());
	}

	private void clearEmailSender(EmailProps props) {
		if (props == null) {
			return;
		}
		EmailMessageType emailMessageType = (EmailMessageType) this.getMessageType(EmailMessageType.TYPE);
		emailMessageType.clearSender(props.getHost(), props.getPort(), props.getUser());
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
