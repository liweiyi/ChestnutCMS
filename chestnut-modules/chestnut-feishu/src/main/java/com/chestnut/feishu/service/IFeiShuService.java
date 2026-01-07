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
package com.chestnut.feishu.service;

import com.chestnut.feishu.domain.dto.FeiShuLoginConfigProps;
import com.chestnut.feishu.domain.dto.GetFeiShuAccessTokenResponse;
import com.chestnut.feishu.domain.dto.GetFeiShuUserInfoResponse;

/**
 * 飞书 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IFeiShuService {

    /**
     * 获取飞书网页登录URL
     *
     * @param configProps 登录配置
     * @param state 透传信息
     * @return 登录URL
     */
    String getLoginURL(FeiShuLoginConfigProps configProps, String state);

    /**
     * 获取飞书用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    GetFeiShuUserInfoResponse.FeiShuUserInfo getUserInfo(String accessToken);

    /**
     * 获取飞书访问令牌
     *
     * @param code 登录授权码
     * @param configProps 登录配置
     * @return 访问令牌
     */
    GetFeiShuAccessTokenResponse getAccessToken(String code, FeiShuLoginConfigProps configProps);
}
