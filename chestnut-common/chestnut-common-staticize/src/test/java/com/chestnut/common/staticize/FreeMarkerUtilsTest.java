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

import com.chestnut.common.utils.DateUtils;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreeMarkerUtilsTest {

    private static final String VARIABLE_NAME = "value";

    private Environment environment;

    @BeforeEach
    void setUp() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
        Template template = new Template("test", "", configuration);
        environment = template.createProcessingEnvironment(Map.of(), new StringWriter());
    }

    @Test
    void shouldReturnNullForMissingVariables() throws TemplateModelException {
        assertNull(FreeMarkerUtils.getStringVariable(environment, VARIABLE_NAME));
        assertNull(FreeMarkerUtils.getIntegerVariable(environment, VARIABLE_NAME));
        assertNull(FreeMarkerUtils.getDateVariable(environment, VARIABLE_NAME));
        assertNull(FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));
    }

    @Test
    void shouldParseSupportedVariableTypes() throws TemplateModelException {
        environment.setVariable(VARIABLE_NAME, new SimpleScalar("栗子CMS"));
        assertEquals("栗子CMS", FreeMarkerUtils.getStringVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleNumber(new BigDecimal("12.50")));
        assertEquals("12.50", FreeMarkerUtils.getStringVariable(environment, VARIABLE_NAME));
        assertEquals(12.5D, FreeMarkerUtils.getDoubleVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("42"));
        assertEquals(42, FreeMarkerUtils.getIntegerVariable(environment, VARIABLE_NAME));

        Date date = Date.from(Instant.parse("2026-07-30T12:34:56Z"));
        environment.setVariable(VARIABLE_NAME, new SimpleDate(date, TemplateDateModel.DATETIME));
        assertEquals(date, FreeMarkerUtils.getDateVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("2026-07-30 12:34:56"));
        assertEquals(DateUtils.parseDate("2026-07-30 12:34:56"),
                FreeMarkerUtils.getDateVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, TemplateBooleanModel.TRUE);
        assertTrue(FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleNumber(0));
        assertFalse(FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("F"));
        assertFalse(FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("yes"));
        assertTrue(FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));
    }

    @Test
    void shouldHandleBlankDateAsNull() throws TemplateModelException {
        environment.setVariable(VARIABLE_NAME, new SimpleScalar(" "));
        assertNull(FreeMarkerUtils.getDateVariable(environment, VARIABLE_NAME));
    }

    @Test
    void shouldRejectIllegalVariableInputs() throws TemplateModelException {
        TemplateModel unsupported = new TemplateModel() {
        };
        environment.setVariable(VARIABLE_NAME, unsupported);
        assertThrows(TemplateModelException.class,
                () -> FreeMarkerUtils.getStringVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("not-a-number"));
        assertThrows(TemplateModelException.class,
                () -> FreeMarkerUtils.getIntegerVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar("not-a-date"));
        assertThrows(TemplateModelException.class,
                () -> FreeMarkerUtils.getDateVariable(environment, VARIABLE_NAME));

        environment.setVariable(VARIABLE_NAME, new SimpleScalar(" "));
        assertThrows(TemplateModelException.class,
                () -> FreeMarkerUtils.getBoolVariable(environment, VARIABLE_NAME));
    }
}
