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
package com.chestnut.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncodingUtilsTest {

    private static final String UTF8_TEXT = "栗子CMS🔐";

    @TempDir
    Path tempDir;

    @Test
    void shouldUseUtf8ForStringDigests() {
        assertEquals("3a5c53804ec105000c1e7e82b9f63780f12fede6", EncryptUtils.sha1(UTF8_TEXT));
        assertEquals("8baa43dc3a9ca87dbf762240ea08e256", EncryptUtils.md5AsHex(UTF8_TEXT));
    }

    @Test
    void shouldRoundTripUtf8TextWithAes() {
        String encrypted = EncryptUtils.encryptAES(UTF8_TEXT, "测试密钥🔐");

        assertNotNull(encrypted);
        assertEquals(UTF8_TEXT, EncryptUtils.decryptAES(encrypted, "测试密钥🔐"));
    }

    @Test
    void shouldWriteAndReadJsonFilesAsUtf8() throws Exception {
        Path jsonFile = tempDir.resolve("utf8.json");
        Map<String, String> value = Map.of("名称", UTF8_TEXT);

        JacksonUtils.toFile(jsonFile.toString(), value);

        assertArrayEquals(JacksonUtils.to(value).getBytes(StandardCharsets.UTF_8), Files.readAllBytes(jsonFile));
        assertEquals(value, JacksonUtils.from(jsonFile.toFile(), Map.class));
    }

    @Test
    void shouldExtractJsonTextBytesAsUtf8() {
        String json = "{\"value\":\"" + UTF8_TEXT + "\"}";

        assertArrayEquals(UTF8_TEXT.getBytes(StandardCharsets.UTF_8), JacksonUtils.getAsBytes(json, "value"));
    }

    @Test
    void shouldAddNullTypedAndFallbackValuesToJson() {
        assertEquals("栗子", JacksonUtils.getAsString(JacksonUtils.add("{}", "value", "栗子"), "value"));
        assertEquals(42, JacksonUtils.getAsInt(JacksonUtils.add("{}", "value", 42), "value"));
        assertEquals(0, new BigDecimal("12.50").compareTo(
                JacksonUtils.getAsBigDecimal(JacksonUtils.add("{}", "value", new BigDecimal("12.50")), "value")));
        assertEquals("null", JacksonUtils.getAsString(JacksonUtils.add("{}", "value", null), "value"));
        assertEquals("{\"名称\":\"栗子\"}",
                JacksonUtils.getAsString(JacksonUtils.add("{}", "value", Map.of("名称", "栗子")), "value"));
        assertThrows(JacksonException.class, () -> JacksonUtils.add("[]", "value", 1));
    }
}
