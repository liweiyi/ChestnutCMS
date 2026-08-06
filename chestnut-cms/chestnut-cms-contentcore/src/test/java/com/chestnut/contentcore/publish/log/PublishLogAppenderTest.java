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
package com.chestnut.contentcore.publish.log;

import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublishLogAppenderTest {

    @Test
    void shouldDecodeEncoderOutputAsUtf8() {
        String message = "发布日志🔐";
        PublishLogAppender<String> appender = new PublishLogAppender<>();
        appender.setEncoder(utf8Encoder(message));
        appender.start();

        appender.doAppend("event");

        assertEquals(message, appender.getLogsSince(0).get(0).message());
    }

    @Test
    void shouldFollowConfiguredEncoderCharset() {
        String message = "可配置发布日志🔐";
        PublishLogAppender<String> appender = new PublishLogAppender<>();
        appender.setEncoder(layoutEncoder(message));
        appender.start();

        appender.doAppend("event");

        assertEquals(message, appender.getLogsSince(0).get(0).message());
    }

    private static EncoderBase<String> utf8Encoder(String message) {
        return new EncoderBase<>() {
            @Override
            public byte[] headerBytes() {
                return new byte[0];
            }

            @Override
            public byte[] encode(String event) {
                return message.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public byte[] footerBytes() {
                return new byte[0];
            }
        };
    }

    private static LayoutWrappingEncoder<String> layoutEncoder(String message) {
        LayoutWrappingEncoder<String> encoder = new LayoutWrappingEncoder<>();
        encoder.setCharset(StandardCharsets.UTF_16LE);
        encoder.setLayout(new LayoutBase<>() {
            @Override
            public String doLayout(String event) {
                return message;
            }
        });
        return encoder;
    }
}
