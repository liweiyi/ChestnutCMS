/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.feishu.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.feishu.domain.dto.FeiShuLoginConfigProps;
import com.chestnut.feishu.domain.dto.GetFeiShuAccessTokenResponse;
import com.chestnut.feishu.domain.dto.GetFeiShuUserInfoResponse;
import com.chestnut.feishu.service.IFeiShuService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeiShuServiceImpl implements IFeiShuService {

    @Override
    public String getLoginURL(FeiShuLoginConfigProps configProps, String state) {
        String apiUrl = "https://accounts.feishu.cn/open-apis/authen/v1/authorize";

        Map<String, Object> params = Map.of(
                "client_id", configProps.getAppId(),
                "redirect_uri", URLEncoder.encode(configProps.getRedirectUri(),  StandardCharsets.UTF_8),
                "response_type", "code",
                "scope", configProps.getScope(),
                "state", state
        );
        return ServletUtils.appendUrlQueryParams(apiUrl, params);
    }

    @Override
    public GetFeiShuUserInfoResponse.FeiShuUserInfo getUserInfo(String accessToken) {
        // 拉取微信用户信息
        String url = "https://open.feishu.cn/open-apis/authen/v1/user_info";
        String res = HttpUtils.get(URI.create(url), Map.of(
                "Authorization", "Bearer " + accessToken,
            "Content-Type", "application/json; charset=utf-8"
        ));
        int errcode = JacksonUtils.getAsInt(res, "errcode");
        if (errcode != 0) {
            throw new GlobalException(JacksonUtils.getAsString(res, "errmsg"));
        }
        GetFeiShuUserInfoResponse response = JacksonUtils.from(res, GetFeiShuUserInfoResponse.class);
        return response.getData();
    }

    @Override
    public GetFeiShuAccessTokenResponse getAccessToken(String code, FeiShuLoginConfigProps configProps) {
        String url = "https://open.feishu.cn/open-apis/authen/v2/oauth/token";
        ObjectNode body = JacksonUtils.objectNode();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("client_id", configProps.getAppId());
        body.put("client_secret", configProps.getAppSecret());
        body.put("redirect_uri", configProps.getRedirectUri());
        try {
            String res = HttpUtils.post(URI.create(url), body.toString(), Map.of(
                    "Content-Type", "application/json; charset=utf-8"
            ));

            int errcode = JacksonUtils.getAsInt(res, "code");
            if (errcode != 0) {
                throw new RuntimeException(JacksonUtils.getAsString(res, "error") + ", " + JacksonUtils.getAsString(res, "error_description"));
            }
            return JacksonUtils.from(res, GetFeiShuAccessTokenResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
