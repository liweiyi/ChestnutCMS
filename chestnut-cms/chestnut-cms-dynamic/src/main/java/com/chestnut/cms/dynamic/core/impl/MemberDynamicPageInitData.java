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
package com.chestnut.cms.dynamic.core.impl;

import com.chestnut.cms.dynamic.core.IDynamicPageInitData;
import com.chestnut.cms.member.CmsMemberConstants;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.util.MemberUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MemberDynamicPageInitData
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageInitData.BEAN_PREFIX + MemberDynamicPageInitData.TYPE)
public class MemberDynamicPageInitData implements IDynamicPageInitData {

    public static final String TYPE = "Member";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{DYNAMIC_PAGE_INIT_DATA." + TYPE + "}";
    }

    @Override
    public void initTemplateData(TemplateContext context, Map<String, String> parameters) {
        if (StpMemberUtil.isLogin()) {
            LoginUser loginUser = StpMemberUtil.getLoginUser();
            context.getVariables().put(CmsMemberConstants.TEMPLATE_VARIABLE_MEMBER, loginUser.getUser());
            context.getVariables().put(CmsMemberConstants.TEMPLATE_VARIABLE_MEMBER_RESOURCE_PREFIX,
                    MemberUtils.getMemberResourcePrefix(context.isPreview()));
        }
    }
}
