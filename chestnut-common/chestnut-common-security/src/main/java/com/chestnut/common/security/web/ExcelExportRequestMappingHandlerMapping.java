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
package com.chestnut.common.security.web;

import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.aspectj.ExcelExportAspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 添加导出TableData数据为Excel的请求处理器映射
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class ExcelExportRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    public int getOrder() {
        return -10;
    }

    @Override
    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = (handler instanceof String beanName ?
                obtainApplicationContext().getType(beanName) : handler.getClass());
        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                        try {
                            return getMappingForMethod(method, userType);
                        }
                        catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });
            methods.forEach((method, mapping) -> {
                if (method.isAnnotationPresent(ExcelExportable.class)) {
                    Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                    RequestMappingInfo combine = mapping.combine(
                            RequestMappingInfo.paths()
                                    .methods(RequestMethod.POST)
                                    .headers(ExcelExportAspect.CONDITION_HEADER)
                                    .options(this.getBuilderConfiguration())
                                    .build()
                    );
                    registerHandlerMethod(handler, invocableMethod, combine);
                }
            });
        }
    }
}
