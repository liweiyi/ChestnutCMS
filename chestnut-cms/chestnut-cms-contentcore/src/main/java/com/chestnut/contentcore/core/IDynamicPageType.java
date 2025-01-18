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
import lombok.Getter;
import lombok.Setter;

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

    RequestArg REQUEST_ARG_SITE_ID = new RequestArg("sid", "{DYNAMIC_PAGE_TYPE.ARG.sid}", RequestArgType.Parameter, true);
    RequestArg REQUEST_ARG_PUBLISHPIPE_CODE = new RequestArg("pp", "{DYNAMIC_PAGE_TYPE.ARG.pp}", RequestArgType.Parameter, true);
    RequestArg REQUEST_ARG_PREVIEW = new RequestArg("preview", "{DYNAMIC_PAGE_TYPE.ARG.preview}", RequestArgType.Parameter, false, "false");

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

    @Getter
    @Setter
    class RequestArg {
        private String name;
        private String desc;
        private RequestArgType type;
        private boolean mandatory;
        private String defValue;

        public RequestArg(String name, String desc, RequestArgType type, boolean mandatory) {
            this(name, desc, type, mandatory, null);
        }

        public RequestArg(String name, String desc, RequestArgType type, boolean mandatory, String defValue) {
            this.name = name;
            this.desc = desc;
            this.type = type;
            this.mandatory = mandatory;
            this.defValue = defValue;
        }
    }

    enum RequestArgType {
        Parameter, Path
    }
}
