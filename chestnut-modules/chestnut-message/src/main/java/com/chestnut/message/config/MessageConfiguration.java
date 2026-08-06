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
package com.chestnut.message.config;

import com.chestnut.message.mapper.CcMessageTemplateMapper;
import com.chestnut.message.service.IMessageTemplateService;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * MessageConfiguration
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Configuration
public class MessageConfiguration {

    @Bean("messageFreeMarkerTemplateLoader")
    TemplateLoader messageFreeMarkerTemplateLoader(CcMessageTemplateMapper messageTemplateMapper) {
        return new MessageFreeMarkerTemplateLoader(messageTemplateMapper);
    }

    @Bean("messageFreeMakerConfiguration")
    freemarker.template.Configuration messageFreeMakerConfiguration(TemplateLoader messageFreeMarkerTemplateLoader) {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_34);
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        // 模板加载器
        cfg.setTemplateLoader(messageFreeMarkerTemplateLoader);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        // 默认模板缓存策略：Most recently use cache.
        // 缓存分两级，强引用->弱引用，强引用数达到上限则会将使用次数更少的转移到弱引用缓存，强引用不会被JVM释放，弱引用则相反。
        // 默认设置：strongSizeLimit = 100，softSizeLimit = 1000
        cfg.setCacheStorage(new MruCacheStorage(100, 1000));
        cfg.setNumberFormat("0.##");
        // settings
        return cfg;
    }
}
