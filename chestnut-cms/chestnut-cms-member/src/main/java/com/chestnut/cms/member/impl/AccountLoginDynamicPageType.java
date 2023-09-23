package com.chestnut.cms.member.impl;

import com.chestnut.cms.member.publishpipe.PublishPipeProp_MemberLoginTemplate;
import com.chestnut.contentcore.core.IDynamicPageType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会员登录页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageType.BEAN_PREFIX + AccountLoginDynamicPageType.TYPE)
public class AccountLoginDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountLogin";

    public static final String REQUEST_PATH = "account/login";

    public static final List<RequestArg> REQUEST_ARGS =  List.of(
            REQUEST_ARG_SITE_ID,
            REQUEST_ARG_PUBLISHPIPE_CODE,
            REQUEST_ARG_PREVIEW
    );

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "会员登录页";
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
        return PublishPipeProp_MemberLoginTemplate.KEY;
    }
}
