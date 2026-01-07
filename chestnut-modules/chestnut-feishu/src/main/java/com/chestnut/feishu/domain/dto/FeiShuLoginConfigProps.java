package com.chestnut.feishu.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * FeiShuLoginConfigProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class FeiShuLoginConfigProps {
    private String appId;
    private String appSecret;
    private String redirectUri;
    private List<String> scope;
}
