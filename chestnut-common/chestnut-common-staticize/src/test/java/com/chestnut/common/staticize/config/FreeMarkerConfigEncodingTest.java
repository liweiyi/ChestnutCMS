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
package com.chestnut.common.staticize.config;

import com.chestnut.common.staticize.config.properties.FreeMarkerProperties;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreeMarkerConfigEncodingTest {

    private final FreeMarkerConfig config = new FreeMarkerConfig();

    @Test
    void shouldInheritDefaultEncodingWhenOutputEncodingIsMissing() throws Exception {
        FreeMarkerProperties properties = new FreeMarkerProperties();
        properties.setDefaultEncoding(StandardCharsets.UTF_16LE.name());

        Configuration configuration = createConfiguration(properties);

        assertEquals(StandardCharsets.UTF_16LE.name(), configuration.getOutputEncoding());
    }

    @Test
    void shouldAllowOutputEncodingToBeConfiguredIndependently() throws Exception {
        FreeMarkerProperties properties = new FreeMarkerProperties();
        properties.setDefaultEncoding(StandardCharsets.UTF_8.name());
        properties.setOutputEncoding(StandardCharsets.UTF_16BE.name());

        Configuration configuration = createConfiguration(properties);

        assertEquals(StandardCharsets.UTF_8.name(), configuration.getDefaultEncoding());
        assertEquals(StandardCharsets.UTF_16BE.name(), configuration.getOutputEncoding());
    }

    private Configuration createConfiguration(FreeMarkerProperties properties) throws Exception {
        return config.staticizeFreeMarkerConfiguration(properties, List.of(new StringTemplateLoader()));
    }
}
