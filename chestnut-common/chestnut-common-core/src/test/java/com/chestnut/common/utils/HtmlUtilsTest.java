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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlUtilsTest {

    @Test
    void cleanRichTextRemovesExecutableContent() {
        String html = """
                <p style="color:red" onclick="alert(1)">正文</p>
                <script>alert(1)</script>
                <iframe src="https://evil.example"></iframe>
                <img src="javascript:alert(1)" onerror="alert(1)">
                <a href="java&#x73;cript:alert(1)">链接</a>
                """;

        String cleaned = HtmlUtils.cleanRichText(html);

        assertTrue(cleaned.contains("<p>正文</p>"));
        assertFalse(cleaned.contains("script"));
        assertFalse(cleaned.contains("iframe"));
        assertFalse(cleaned.contains("onclick"));
        assertFalse(cleaned.contains("onerror"));
        assertFalse(cleaned.contains("style="));
        assertFalse(cleaned.toLowerCase().contains("javascript:"));
    }

    @Test
    void cleanRichTextKeepsSafeFormattingAndInternalUrls() {
        String html = """
                <figure><img src="iurl://resources/image/a.png?type=resource&id=1"></figure>
                <a href="iurl://content?id=2">站内文章</a>
                <table><tbody><tr><td>单元格</td></tr></tbody></table>
                """;

        String cleaned = HtmlUtils.cleanRichText(html);

        assertTrue(cleaned.contains("<figure>"));
        assertTrue(cleaned.contains("src=\"iurl://resources/image/a.png?type=resource&amp;id=1\""));
        assertTrue(cleaned.contains("href=\"iurl://content?id=2\""));
        assertTrue(cleaned.contains("<table>"));
    }

    @Test
    void cleanRichTextKeepsRelativeUrlsButRejectsDataUrls() {
        String html = """
                <img src="/uploads/image.png">
                <img src="data:image/svg+xml,<svg onload=alert(1)>">
                """;

        String cleaned = HtmlUtils.cleanRichText(html);

        assertTrue(cleaned.contains("src=\"/uploads/image.png\""));
        assertFalse(cleaned.contains("data:image"));
        assertFalse(cleaned.contains("onload"));
    }
}
