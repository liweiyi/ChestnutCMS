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
package com.chestnut.cms.search.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ContentIndexServiceTest {

    @Test
    void shouldNormalizeSupportedDateTypesToUtcEpochMillis() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 7, 30, 12, 34, 56);
        LocalDate date = LocalDate.of(2026, 7, 30);
        Instant instant = Instant.parse("2026-07-30T12:34:56Z");

        assertEquals(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                ContentIndexService.normalizeIndexValue(dateTime));
        assertEquals(date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                ContentIndexService.normalizeIndexValue(date));
        assertEquals(instant.toEpochMilli(), ContentIndexService.normalizeIndexValue(instant));
    }

    @Test
    void shouldPreserveNullAndNonDateValues() {
        Object unsupportedValue = new Object();

        assertNull(ContentIndexService.normalizeIndexValue(null));
        assertSame(unsupportedValue, ContentIndexService.normalizeIndexValue(unsupportedValue));
    }
}
