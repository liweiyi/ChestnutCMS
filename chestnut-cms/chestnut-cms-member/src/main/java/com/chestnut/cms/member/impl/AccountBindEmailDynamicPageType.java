package com.chestnut.cms.member.impl;

import com.chestnut.cms.member.publishpipe.PublishPipeProp_MemberBindEmailTemplate;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.core.IDynamicPageType;
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
 * 会员绑定邮箱页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IDynamicPageType.BEAN_PREFIX + AccountBindEmailDynamicPageType.TYPE)
public class AccountBindEmailDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountBindEmail";

    public static final String REQUEST_PATH = "account/change_email";

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
        return "会员绑定邮箱页";
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
        return PublishPipeProp_MemberBindEmailTemplate.KEY;
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
        templateContext.getVariables().put("Member", member);
        templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(templateContext.isPreview()));
        templateContext.getVariables().put("Request", parameters);
    }
}
