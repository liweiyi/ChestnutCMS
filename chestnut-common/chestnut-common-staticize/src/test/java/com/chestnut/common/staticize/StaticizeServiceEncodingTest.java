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
package com.chestnut.common.staticize;

import com.chestnut.common.staticize.core.TemplateContext;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticizeServiceEncodingTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteStaticFileUsingConfiguredFreeMarkerOutputEncoding() throws Exception {
        String content = "栗子CMS🔐";
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("index", content);

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setOutputEncoding(StandardCharsets.UTF_16LE.name());
        configuration.setTemplateLoader(templateLoader);
        StaticizeService service = new StaticizeService(configuration, List.of(), List.of());

        TemplateContext context = new TemplateContext("index", false, "default");
        context.setDirectory(tempDir + File.separator);
        context.setFirstFileName("index.html");
        service.process(context);

        assertEquals(StandardCharsets.UTF_16LE, service.getOutputCharset());
        assertEquals(content, Files.readString(tempDir.resolve("index.html"), StandardCharsets.UTF_16LE));
    }
}
