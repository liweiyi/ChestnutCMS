/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.common.security.config;

import com.chestnut.common.security.config.properties.ChestnutPageProperties;
import com.chestnut.common.security.web.ExcelExportRequestMappingHandlerMapping;
import com.chestnut.common.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Objects;
import java.util.Set;

/**
 * 分页配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ChestnutPageProperties.class)
public class ChestnutPageConfig {

    private static int startPageNumber;

    private static int defaultPageSize;

    private static int maxPageSize;

    ChestnutPageConfig(ChestnutPageProperties properties) {
        startPageNumber = properties.getStartPageNumber();
        defaultPageSize = properties.getDefaultPageSize();
        maxPageSize = properties.getMaxPageSize();
        checkConflictRequestMapping();
    }

    private void checkConflictRequestMapping() {
        ExcelExportRequestMappingHandlerMapping bean = SpringUtils.getBean(ExcelExportRequestMappingHandlerMapping.class);
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping");
        for (RequestMappingInfo info : bean.getHandlerMethods().keySet()) {
            Set<String> paths = Objects.requireNonNull(info.getDirectPaths());
            paths.forEach(path -> {
                if (hasHandler(mapping, path, RequestMethod.POST)) {
                    throw new ApplicationContextException("Controller handler conflict with excel export handler: [POST]" + path);
                }
            });
        }
    }

    private boolean hasHandler(RequestMappingHandlerMapping mapping, String path, RequestMethod method) {
        return mapping.getHandlerMethods().keySet().stream().anyMatch(info -> {
            Set<String> paths = Objects.requireNonNull(info.getDirectPaths());
            if (!paths.contains(path)) {
                return false;
            }
            return info.getMethodsCondition().getMethods().contains(method);
        });
    }

    public static int getStartPageNumber() {
        return startPageNumber;
    }

    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    public static int getMaxPageSize() {
        return maxPageSize;
    }
}