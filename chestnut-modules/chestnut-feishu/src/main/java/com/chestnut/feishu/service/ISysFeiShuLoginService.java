package com.chestnut.feishu.service;

/**
 * ISysUserWechatLoginService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysFeiShuLoginService {
    String getLoginURL(Long configId);

    String verifyLogin(String code, String state);
}
