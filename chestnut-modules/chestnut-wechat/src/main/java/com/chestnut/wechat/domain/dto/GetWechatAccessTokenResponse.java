package com.chestnut.wechat.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * GetWechatAccessTokenResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class GetWechatAccessTokenResponse {
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;
}
