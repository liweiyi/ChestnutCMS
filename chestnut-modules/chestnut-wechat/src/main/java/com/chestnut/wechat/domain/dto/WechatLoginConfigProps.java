package com.chestnut.wechat.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * WechatLoginConfigProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class WechatLoginConfigProps {
    private String appId;
    private String appSecret;
    private String redirectUri;
    private String style;
    private String href;
    private String stylelite;
    private String fastLogin;
    private String colorScheme;
}
