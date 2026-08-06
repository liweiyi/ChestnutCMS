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
package com.chestnut.member.controller;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.member.domain.MemberConfig;
import com.chestnut.member.domain.dto.UpdateMemberConfigRequest;
import com.chestnut.member.domain.vo.MemberConfigDefinitionVO;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@XComment("{API.DOC.MEMBER.CONFIG_MODULE}")
@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberConfig)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/config")
public class MemberConfigController extends BaseRestController {

    private final IMemberConfigService memberConfigService;

    @XComment("{API.DOC.MEMBER.CONFIG_GET_DETAIL}")
    @GetMapping("/detail")
    public R<MemberConfig> getMemberConfig() {
        return R.ok(this.memberConfigService.getMemberConfig());
    }

    @XComment("{API.DOC.MEMBER.CONFIG_GET_DEFINITIONS}")
    @GetMapping("/definitions")
    public R<List<MemberConfigDefinitionVO>> getConfigDefinitions() {
        return R.ok(this.memberConfigService.getConfigDefinitions());
    }

    @XComment("{API.DOC.MEMBER.CONFIG_UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberConfigEdit)
    @Log(title = "编辑会员配置", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public R<Void> updateMemberConfig(@RequestBody @Validated @XComment("{API.DOC.MEMBER.CONFIG_UPDATE_REQUEST}") UpdateMemberConfigRequest req) {
        this.memberConfigService.updateMemberConfig(req);
        return R.ok();
    }
}
