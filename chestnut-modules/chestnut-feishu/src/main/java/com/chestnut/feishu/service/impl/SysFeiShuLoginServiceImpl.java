package com.chestnut.feishu.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.feishu.FeiShuLoginType;
import com.chestnut.feishu.domain.dto.FeiShuLoginConfigProps;
import com.chestnut.feishu.domain.dto.GetFeiShuAccessTokenResponse;
import com.chestnut.feishu.domain.dto.GetFeiShuUserInfoResponse;
import com.chestnut.feishu.service.IFeiShuService;
import com.chestnut.feishu.service.ISysFeiShuLoginService;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserBinding;
import com.chestnut.system.domain.dto.CreateBindingUserRequest;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.service.ILoginConfigService;
import com.chestnut.system.service.ISysUserBindingService;
import com.chestnut.system.service.ISysUserService;
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
public class SysFeiShuLoginServiceImpl implements ISysFeiShuLoginService {

    private static final String CACHE_PREFIX = "cc:feishu:state:";

    private final RedisCache redisCache;

    private final ILoginConfigService loginConfigService;

    private final IFeiShuService feiShuService;

    private final ISysUserService userService;

    private final ISysUserBindingService userBindingService;

    private final SysLoginService loginService;

    private final RedissonClient redissonClient;

    @Override
    public String getLoginURL(Long configId) {
        SysLoginConfig loginConfig = loginConfigService.getLoginConfig(configId);
        if (!FeiShuLoginType.TYPE.equals(loginConfig.getType())) {
            throw new GlobalException("Invalid login type: " + loginConfig.getType());
        }
        if (EnableOrDisable.isDisable(loginConfig.getStatus())) {
            throw new GlobalException("Login config is disabled!");
        }
        FeiShuLoginConfigProps configProps = JacksonUtils.convertValue(loginConfig.getConfigProps(),
                FeiShuLoginConfigProps.class);
        if (Objects.isNull(configProps)) {
            throw new GlobalException("Invalid login config props!");
        }
        String state = IdUtils.randomUUID() + "-" + configId;
        redisCache.setCacheObject(CACHE_PREFIX + state, state, 300, TimeUnit.SECONDS);
        return this.feiShuService.getLoginURL(configProps, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyLogin(String code, String state) {
        RLock lock = redissonClient.getLock("FeiShuLogin-" + state);
        lock.lock();
        try {
            if (!redisCache.hasKey(CACHE_PREFIX + state)) {
                throw new GlobalException("Access deny!");
            }
            redisCache.deleteObject(CACHE_PREFIX + state);
            String configId = StringUtils.substringAfterLast(state, "-");

            SysLoginConfig loginConfig = loginConfigService.getLoginConfig(Long.parseLong(configId));
            if (!FeiShuLoginType.TYPE.equals(loginConfig.getType())) {
                throw new GlobalException("Invalid login type: " + loginConfig.getType());
            }
            if (EnableOrDisable.isDisable(loginConfig.getStatus())) {
                throw new GlobalException("Login config is disabled!");
            }
            FeiShuLoginConfigProps configProps = JacksonUtils.convertValue(loginConfig.getConfigProps(),
                    FeiShuLoginConfigProps.class);
            if (Objects.isNull(configProps)) {
                throw new GlobalException("Invalid login config props!");
            }
            // 获取accessToken
            GetFeiShuAccessTokenResponse accessTokenResponse = this.feiShuService.getAccessToken(code, configProps);
            // 获取用户信息
            GetFeiShuUserInfoResponse.FeiShuUserInfo userInfo = this.feiShuService.getUserInfo(accessTokenResponse.getAccess_token());

            // 查找openId一致的后台用户
            SysUser user = null;
            Long userId = userBindingService.getUserId(FeiShuLoginType.TYPE, userInfo.open_id());
            if (IdUtils.validate(userId)) {
                user = userService.getById(userId);
            }
            if (Objects.isNull(user)) {
                user = this.createUser(accessTokenResponse);
                SysUserBinding binding = new SysUserBinding();
                binding.setBindingId(IdUtils.getSnowflakeId());
                binding.setUserId(user.getUserId());
                binding.setBindingType(FeiShuLoginType.TYPE);
                binding.setOpenId(userInfo.open_id());
                binding.setUnionId(userInfo.union_id());
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

    private SysUser createUser(GetFeiShuAccessTokenResponse accessTokenResponse) {
        // 获取微信用户信息
        GetFeiShuUserInfoResponse.FeiShuUserInfo userInfo = this.feiShuService.getUserInfo(accessTokenResponse.getAccess_token());
        CreateBindingUserRequest req = new CreateBindingUserRequest();
        req.setUserName("wx_" + IdUtils.getSnowflakeIdStr());
        req.setNickName(userInfo.name());
        req.setAvatar(userInfo.avatar_url());
        return this.userService.createBindingUser(req);
    }
}
