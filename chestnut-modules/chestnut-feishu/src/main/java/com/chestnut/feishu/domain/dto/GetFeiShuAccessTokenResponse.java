package com.chestnut.feishu.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * GetFeiShuAccessTokenResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class GetFeiShuAccessTokenResponse {
    private Integer code;
    private String error;
    private String error_description;
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private Long refresh_token_expires_in;
    private String token_type;
    private String scope;
}
