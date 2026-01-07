package com.chestnut.system.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * OAuthCallbackReq
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class OAuthCallbackReq {

    private String redirectUrl;

    private String code;


}
