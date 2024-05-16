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
package com.chestnut.cms.member.impl;

import com.chestnut.cms.member.CmsMemberConstants;
import com.chestnut.cms.member.publishpipe.PublishPipeProp_MemberSettingTemplate;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.core.IDynamicPageType;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.service.IMemberService;
import com.chestnut.member.service.IMemberStatDataService;
import com.chestnut.member.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 会员设置页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IDynamicPageType.BEAN_PREFIX + AccountSettingDynamicPageType.TYPE)
public class AccountSettingDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountSetting";

    public static final String REQUEST_PATH = "account/setting";

    public static final List<RequestArg> REQUEST_ARGS =  List.of(
            REQUEST_ARG_SITE_ID,
            REQUEST_ARG_PUBLISHPIPE_CODE,
            REQUEST_ARG_PREVIEW
    );

    private final IMemberStatDataService memberStatDataService;

    private final IMemberService memberService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{DYNAMIC_PAGE_TYPE.NAME." + TYPE + "}";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public List<RequestArg> getRequestArgs() {
        return REQUEST_ARGS;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_MemberSettingTemplate.KEY;
    }

    @Override
    public void validate(Map<String, String> parameters) {
        Long memberId = MapUtils.getLong(parameters, "memberId");

        MemberCache member = this.memberStatDataService.getMemberCache(memberId);
        if (Objects.isNull(member)) {
            throw new RuntimeException("Member not found: " + memberId);
        }
    }

    @Override
    public void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {
        Long memberId = MapUtils.getLong(parameters, "memberId");
        Member member = this.memberService.getById(memberId);
        templateContext.getVariables().put(CmsMemberConstants.TEMPLATE_VARIABLE_MEMBER, member);
        templateContext.getVariables().put(CmsMemberConstants.TEMPLATE_VARIABLE_MEMBER_RESOURCE_PREFIX,
                MemberUtils.getMemberResourcePrefix(templateContext.isPreview()));
        templateContext.getVariables().put(TemplateUtils.TemplateVariable_Request, parameters);
    }
}
