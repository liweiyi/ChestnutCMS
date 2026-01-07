package com.chestnut.feishu.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.feishu.service.ISysFeiShuLoginService;
import com.chestnut.system.fixed.config.BackendContext;
import com.chestnut.system.fixed.config.LoginRedirectURL;
import com.chestnut.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SysFeiShuLoginController
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/feishu")
public class SysFeiShuLoginController extends BaseRestController {

    private final ISysFeiShuLoginService feiShuLoginService;

    @GetMapping("/login")
    public R<?> login(@LongId Long configId) {
        String loginURL = feiShuLoginService.getLoginURL(configId);
        String backend = BackendContext.getValue();
        String origin = StringUtils.substringBefore(StringUtils.substringAfter(backend, "://"), "/");
        return R.ok(Map.of(
                "url", loginURL,
                "origin", origin
        ));
    }

    @GetMapping("/login/callback")
    public String login(@RequestParam String code, @RequestParam String state) {
        String token = this.feiShuLoginService.verifyLogin(code, state);
        String loginRedirectUrl = LoginRedirectURL.getValue();
        if (loginRedirectUrl.contains("?")) {
            loginRedirectUrl += "&";
        } else {
            loginRedirectUrl += "?";
        }
        loginRedirectUrl += "token=" + token;
        log.info("wechat.callback.redirect: {}", loginRedirectUrl);
        return """
                <html>
                <head>
                <title>登录成功</title>
                <script>
                window.location.href = "%s";
                </script>
                </head>
                <body>
                登录成功，正在跳转。。。
                </body>
                </html>
                """.formatted(loginRedirectUrl);
    }
}
