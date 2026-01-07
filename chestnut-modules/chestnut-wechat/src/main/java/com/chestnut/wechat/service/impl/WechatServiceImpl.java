/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.wechat.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.wechat.WechatLoginType;
import com.chestnut.system.service.ILoginConfigService;
import com.chestnut.wechat.domain.dto.GetWechatAccessTokenResponse;
import com.chestnut.wechat.domain.dto.GetWechatUserInfoResponse;
import com.chestnut.wechat.domain.dto.WechatLoginConfigProps;
import com.chestnut.wechat.service.IWechatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements IWechatService {

    private static final String CACHE_PREFIX = "cc:login:wechat:state:";

    private final RedisCache redisCache;

    private final ILoginConfigService loginConfigService;

    private final WechatLoginType wechatLoginType;

    @Override
    public String getLoginURL(WechatLoginConfigProps configProps, String state) {
        return "https://open.weixin.qq.com/connect/qrconnect?appid=" + configProps.getAppId()
                + "&redirect_uri=" + URLEncoder.encode(configProps.getRedirectUri(),  StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_login&state=" + state
                + "#wechat_redirect";
    }

    @Override
    public GetWechatUserInfoResponse getUserInfo(String accessToken, String openId) {
        // 拉取微信用户信息
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid="+ openId;
        String res = HttpUtils.get(URI.create(url));
        int errcode = JacksonUtils.getAsInt(res, "errcode");
        if (errcode != 0) {
            throw new GlobalException(JacksonUtils.getAsString(res, "errmsg"));
        }
        return JacksonUtils.from(res, GetWechatUserInfoResponse.class);
    }

    @Override
    public GetWechatAccessTokenResponse getAccessToken(String code, WechatLoginConfigProps configProps) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + configProps.getAppId()
                + "&secret=" + configProps.getAppSecret()
                + "&code=" + code
                + "&grant_type=authorization_code";
        String res = HttpUtils.get(URI.create(url));
        int errcode = JacksonUtils.getAsInt(res, "errcode");
        if (errcode != 0) {
            throw new RuntimeException(JacksonUtils.getAsString(res, "errmsg"));
        }
        return JacksonUtils.from(res, GetWechatAccessTokenResponse.class);
    }
}
