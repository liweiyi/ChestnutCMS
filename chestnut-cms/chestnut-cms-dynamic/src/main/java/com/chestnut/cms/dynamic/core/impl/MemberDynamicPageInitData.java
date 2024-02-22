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
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.util.MemberUtils;
import org.springframework.stereotype.Component;

/**
 * MemberDynamicPageInitData
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class MemberDynamicPageInitData implements IDynamicPageInitData {

    @Override
    public String getType() {
        return "Member";
    }

    @Override
    public String getName() {
        return "登录会员";
    }

    @Override
    public void initTemplateData(TemplateContext context) {
        if (StpMemberUtil.isLogin()) {
            LoginUser loginUser = StpMemberUtil.getLoginUser();
            context.getVariables().put("Member", loginUser.getUser());
            context.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(context.isPreview()));
        }
    }
}
