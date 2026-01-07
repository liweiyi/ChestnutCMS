package com.chestnut.wechat.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * GetWechatUserInfoResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class GetWechatUserInfoResponse {
    public String openid;
    public String nickname;
    public Integer sex;
    public String province;
    public String city;
    public String country;
    public String headimgurl;
    public List<String> privilege;
    public String unionid;
}
