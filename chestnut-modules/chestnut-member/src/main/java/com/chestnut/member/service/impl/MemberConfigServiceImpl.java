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
package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.member.config.IMemberConfig;
import com.chestnut.member.domain.MemberConfig;
import com.chestnut.member.domain.dto.UpdateMemberConfigRequest;
import com.chestnut.member.domain.vo.MemberConfigDefinitionVO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.mapper.MemberConfigMapper;
import com.chestnut.member.service.IMemberConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberConfigServiceImpl extends ServiceImpl<MemberConfigMapper, MemberConfig>
        implements IMemberConfigService {

    private static final Long CONFIG_ID = 1L;

    private static final String CACHE_KEY = "member:config";

    private final RedisCache redisCache;

    private final List<IMemberConfig<?>> memberConfigs;

    @Override
    public MemberConfig getMemberConfig() {
        MemberConfig config = this.redisCache.getCacheObject(CACHE_KEY, MemberConfig.class, () -> {
            MemberConfig dbConfig = this.getById(CONFIG_ID);
            return dbConfig == null ? this.createDefaultConfig() : dbConfig;
        });
        if (config == null) {
            config = this.createDefaultConfig();
        } else {
            ObjectNode configs = config.getConfigs().deepCopy();
            this.normalize(configs);
            config.setConfigs(configs);
        }
        return config;
    }

    @Override
    public List<MemberConfigDefinitionVO> getConfigDefinitions() {
        return this.memberConfigs.stream()
                .sorted(Comparator.comparingInt(IMemberConfig::getOrder))
                .map(MemberConfigDefinitionVO::of)
                .toList();
    }

    @Override
    public <T> T getConfigValue(IMemberConfig<T> memberConfig) {
        return memberConfig.getValue(this.getMemberConfig().getConfigs());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberConfig(UpdateMemberConfigRequest req) {
        ObjectNode configs = req.getConfigs().deepCopy();
        this.normalize(configs);

        MemberConfig config = this.getById(CONFIG_ID);
        if (config == null) {
            config = new MemberConfig();
            config.setConfigId(CONFIG_ID);
            config.setConfigs(configs);
            config.createBy(req.getOperator().getUsername());
            this.save(config);
        } else {
            config.setConfigs(configs);
            config.updateBy(req.getOperator().getUsername());
            this.updateById(config);
        }
        this.redisCache.deleteObject(CACHE_KEY);
    }

    private MemberConfig createDefaultConfig() {
        MemberConfig config = new MemberConfig();
        config.setConfigId(CONFIG_ID);
        this.normalize(config.getConfigs());
        return config;
    }

    private void normalize(ObjectNode configs) {
        for (IMemberConfig<?> memberConfig : this.memberConfigs) {
            this.normalizeConfigValue(configs, memberConfig);
        }
    }

    private <T> void normalizeConfigValue(ObjectNode configs, IMemberConfig<T> memberConfig) {
        try {
            T value = memberConfig.getValue(configs);
            if (!memberConfig.validate(value)) {
                throw MemberErrorCode.INVALID_CONFIG_VALUE.exception(I18nUtils.get(memberConfig.getName()));
            }
            configs.set(memberConfig.getId(), JacksonUtils.getObjectMapper().valueToTree(value));
            memberConfig.getDeprecatedIds().forEach(configs::remove);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw MemberErrorCode.INVALID_CONFIG_VALUE.exception(I18nUtils.get(memberConfig.getName()));
        }
    }
}
