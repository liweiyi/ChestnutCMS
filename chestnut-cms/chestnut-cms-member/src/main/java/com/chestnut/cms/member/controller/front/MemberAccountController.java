/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.cms.member.controller.front;

import com.chestnut.cms.member.impl.*;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.service.impl.DynamicPageService;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 会员个人中心
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberAccountController extends BaseRestController {

    private final DynamicPageService dynamicPageService;

    @GetMapping(AccountCentreDynamicPageType.REQUEST_PATH)
    public void accountCentre(@PathVariable @LongId Long memberId,
                              @RequestParam(value = "type", required = false, defaultValue = "") String type,
                              @RequestParam("sid") Long siteId,
                              @RequestParam("pp") String publishPipeCode,
                              @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
                              @RequestParam(required = false, defaultValue = "false") Boolean preview,
                              HttpServletResponse response)
            throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();
        parameters.put("memberId", memberId.toString());

        this.dynamicPageService.generateDynamicPage(AccountCentreDynamicPageType.TYPE,
                siteId, publishPipeCode, preview, parameters, response);
    }

    @GetMapping(AccountLoginDynamicPageType.REQUEST_PATH)
    public void memberLogin(@RequestParam("sid") Long siteId,
                            @RequestParam("pp") String publishPipeCode,
                            @RequestParam(required = false, defaultValue = "false") Boolean preview,
                            HttpServletResponse response)
            throws IOException {

        this.dynamicPageService.generateDynamicPage(AccountLoginDynamicPageType.TYPE,
                siteId, publishPipeCode, preview, ServletUtils.getParameters(), response);
    }

    @GetMapping(AccountRegisterDynamicPageType.REQUEST_PATH)
    public void memberRegister(@RequestParam("sid") Long siteId,
                            @RequestParam("pp") String publishPipeCode,
                            @RequestParam(required = false, defaultValue = "false") Boolean preview,
                            HttpServletResponse response)
            throws IOException {

        this.dynamicPageService.generateDynamicPage(AccountRegisterDynamicPageType.TYPE,
                siteId, publishPipeCode, preview, ServletUtils.getParameters(), response);
    }

    @GetMapping(AccountForgetPasswordDynamicPageType.REQUEST_PATH)
    public void memberResetPassword(@RequestParam("sid") Long siteId,
                                    @RequestParam("pp") String publishPipeCode,
                                    @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                    HttpServletResponse response) throws IOException {

        this.dynamicPageService.generateDynamicPage(AccountForgetPasswordDynamicPageType.TYPE,
                siteId, publishPipeCode, preview, ServletUtils.getParameters(), response);
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping(AccountSettingDynamicPageType.REQUEST_PATH)
    public void accountSetting(@RequestParam Long sid,
                               @RequestParam String pp,
                               @RequestParam(required = false, defaultValue = "false") Boolean preview,
                               HttpServletResponse response) throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();
        parameters.put("memberId", StpMemberUtil.getLoginIdAsString());

        this.dynamicPageService.generateDynamicPage(AccountSettingDynamicPageType.TYPE,
                sid, pp, preview, parameters, response);
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping(AccountPasswordDynamicPageType.REQUEST_PATH)
    public void accountPassword(@RequestParam Long sid,
                                @RequestParam String pp,
                                @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                HttpServletResponse response)
            throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();
        parameters.put("memberId", StpMemberUtil.getLoginIdAsString());

        this.dynamicPageService.generateDynamicPage(AccountPasswordDynamicPageType.TYPE,
                sid, pp, preview, parameters, response);
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping(AccountBindEmailDynamicPageType.REQUEST_PATH)
    public void accountChangeEmail(@RequestParam Long sid,
                                   @RequestParam String pp,
                                   @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                   HttpServletResponse response)
            throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();
        parameters.put("memberId", StpMemberUtil.getLoginIdAsString());

        this.dynamicPageService.generateDynamicPage(AccountBindEmailDynamicPageType.TYPE,
                sid, pp, preview, parameters, response);
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping(AccountContributeDynamicPageType.REQUEST_PATH)
    public void accountContribute(@RequestParam Long sid,
                                  @RequestParam String pp,
                                  @RequestParam(value = "cid", required = false) Long contentId,
                                  @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                  HttpServletResponse response)
            throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();
        parameters.put("memberId", StpMemberUtil.getLoginIdAsString());

        this.dynamicPageService.generateDynamicPage(AccountContributeDynamicPageType.TYPE,
                sid, pp, preview, parameters, response);
    }
}

