package com.chestnut.wechat.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserBinding;
import com.chestnut.system.domain.dto.CreateBindingUserRequest;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.wechat.WechatLoginType;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.service.ILoginConfigService;
import com.chestnut.system.service.ISysUserBindingService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.wechat.domain.dto.GetWechatAccessTokenResponse;
import com.chestnut.wechat.domain.dto.GetWechatUserInfoResponse;
import com.chestnut.wechat.domain.dto.WechatLoginConfigProps;
import com.chestnut.wechat.service.ISysUserWechatLoginService;
import com.chestnut.wechat.service.IWechatService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * SysUserWechatLoginServiceImpl
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserWechatLoginServiceImpl implements ISysUserWechatLoginService {

    private static final String CACHE_PREFIX = "cc:wechat:state:";

    private final RedisCache redisCache;

    private final ILoginConfigService loginConfigService;

    private final IWechatService wechatService;

    private final ISysUserService userService;

    private final ISysUserBindingService userBindingService;

    private final SysLoginService loginService;

    private final RedissonClient redissonClient;

    @Override
    public String getLoginURL(Long configId) {
        SysLoginConfig loginConfig = loginConfigService.getLoginConfig(configId);
        if (!WechatLoginType.TYPE.equals(loginConfig.getType())) {
            throw new GlobalException("Invalid login type: " + loginConfig.getType());
        }
        if (EnableOrDisable.isDisable(loginConfig.getStatus())) {
            throw new GlobalException("Login config is disabled!");
        }
        WechatLoginConfigProps configProps = JacksonUtils.convertValue(loginConfig.getConfigProps(),
                WechatLoginConfigProps.class);
        if (Objects.isNull(configProps)) {
            throw new GlobalException("Invalid login config props!");
        }
        String state = IdUtils.randomUUID() + "-" + configId;
        redisCache.setCacheObject(CACHE_PREFIX + state, state, 300, TimeUnit.SECONDS);
        return this.wechatService.getLoginURL(configProps, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyLogin(String code, String state) {
        RLock lock = redissonClient.getLock("WechatLogin-" + state);
        lock.lock();
        try {
            if (!redisCache.hasKey(CACHE_PREFIX + state)) {
                throw new GlobalException("Access deny!");
            }
            redisCache.deleteObject(CACHE_PREFIX + state);
            String configId = StringUtils.substringAfterLast(state, "-");

            SysLoginConfig loginConfig = loginConfigService.getLoginConfig(Long.parseLong(configId));
            if (!WechatLoginType.TYPE.equals(loginConfig.getType())) {
                throw new GlobalException("Invalid login type: " + loginConfig.getType());
            }
            if (EnableOrDisable.isDisable(loginConfig.getStatus())) {
                throw new GlobalException("Login config is disabled!");
            }
            WechatLoginConfigProps configProps = JacksonUtils.convertValue(loginConfig.getConfigProps(),
                    WechatLoginConfigProps.class);
            if (Objects.isNull(configProps)) {
                throw new GlobalException("Invalid login config props!");
            }
            // 获取accessToken
            GetWechatAccessTokenResponse accessTokenResponse = this.wechatService.getAccessToken(code, configProps);
            // 查找openId一致的后台用户
            SysUser user = null;
            Long userId = userBindingService.getUserId(WechatLoginType.TYPE, accessTokenResponse.getOpenid());
            if (IdUtils.validate(userId)) {
                user = userService.getById(userId);
            }
            if (Objects.isNull(user)) {
                user = this.createUser(accessTokenResponse);
                SysUserBinding binding = new SysUserBinding();
                binding.setBindingId(IdUtils.getSnowflakeId());
                binding.setUserId(user.getUserId());
                binding.setBindingType(WechatLoginType.TYPE);
                binding.setOpenId(accessTokenResponse.getOpenid());
                binding.setUnionId(accessTokenResponse.getUnionid());
                ObjectNode properties = JacksonUtils.objectNode();
                properties.put("accessToken", accessTokenResponse.getAccess_token());
                properties.put("refreshToken", accessTokenResponse.getRefresh_token());
                properties.put("expiresTime", Instant.now().toEpochMilli() + accessTokenResponse.getExpires_in() * 1000);
                binding.setProperties(properties);
                binding.createBy(SysConstants.SYS_OPERATOR);
                userBindingService.save(binding);
            }
            return loginService.userLogin(user);
        } finally {
            lock.unlock();
        }
    }

    private SysUser createUser(GetWechatAccessTokenResponse accessTokenResponse) {
        // 获取微信用户信息
        GetWechatUserInfoResponse userInfo = this.wechatService.getUserInfo(accessTokenResponse.getAccess_token(),
                accessTokenResponse.getOpenid());
        CreateBindingUserRequest req = new CreateBindingUserRequest();
        req.setUserName("wx_" + IdUtils.getSnowflakeIdStr());
        req.setNickName(userInfo.getNickname());
        req.setAvatar(userInfo.getHeadimgurl());
        if (userInfo.getSex() == 1) {
            req.setSex(Gender.MALE);
        } else if (userInfo.getSex() == 2) {
            req.setSex(Gender.FEMAIL);
        } else {
            req.setSex(Gender.UNKNOWN);
        }
        return this.userService.createBindingUser(req);
    }
}
