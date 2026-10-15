/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.message.controller;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.message.domain.dto.SendSmsLoginCodeReq;
import com.chestnut.message.service.impl.SysSmsLoginService;
import com.chestnut.system.domain.vo.SmsLoginCodeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台短信登录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.LOGIN.API}")
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysSmsLoginController extends BaseRestController {

    private final SysSmsLoginService smsLoginService;

    @XComment("{API.DOC.SYS.LOGIN.SEND_SMS_CODE}")
    @PostMapping("/login/sms/code")
    public R<SmsLoginCodeVO> sendSmsCode(@Validated @RequestBody SendSmsLoginCodeReq request) {
        SmsLoginCodeVO vo = this.smsLoginService.sendCode(request);
        return R.ok(vo);
    }
}
