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
package com.chestnut.contentcore.core;

import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 动态页面类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IDynamicPageType {

    String BEAN_PREFIX = "DynamicPageType_";

    RequestArg REQUEST_ARG_SITE_ID = new RequestArg("sid", "站点ID", RequestArgType.Parameter, true, null);
    RequestArg REQUEST_ARG_PUBLISHPIPE_CODE = new RequestArg("pp", "发布通道编码", RequestArgType.Parameter, true, null);
    RequestArg REQUEST_ARG_PREVIEW = new RequestArg("preview", "是否预览模式", RequestArgType.Parameter, false, "false");

    /**
     * 类型
     */
    String getType();

    /**
     * 名称
     */
    String getName();

    /**
     * 描述
     * @return
     */
    default String getDesc() {
        return StringUtils.EMPTY;
    }

    /**
     * 访问路径
     */
    String getRequestPath();

    /**
     * 模板发布通道配置属性KEY
     */
    String getPublishPipeKey();

    /**
     * 请求参数
     */
    default List<RequestArg> getRequestArgs() {
        return List.of();
    }

    /**
     * 校验请求参数
     *
     * @param parameters
     */
    default void validate(Map<String, String> parameters) {

    }

    /**
     * 模板初始化数据
     *
     * @param parameters
     * @param templateContext
     */
    default void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {

    }

    record RequestArg(
            String name, // 参数名

            String desc, // 参数说明

            RequestArgType type, // 类型：parameter, path

            boolean mandatory, // 是否必填

            String defaultValue // 默认值
    ){}

    enum RequestArgType {
        Parameter, Path
    }
}
