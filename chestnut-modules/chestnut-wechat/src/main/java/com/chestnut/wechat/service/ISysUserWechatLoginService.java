package com.chestnut.wechat.service;

/**
 * ISysUserWechatLoginService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysUserWechatLoginService {
    String getLoginURL(Long configId);

    String verifyLogin(String code, String state);
}
