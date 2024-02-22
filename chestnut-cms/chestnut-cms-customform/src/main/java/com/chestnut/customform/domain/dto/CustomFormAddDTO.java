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
package com.chestnut.customform.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义表单添加DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CustomFormAddDTO extends BaseDTO {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 编码
     */
    @NotBlank
    private String code;

    /**
     * 模型数据表
     */
    @NotBlank
    private String tableName;

    /**
     * 是否需要验证码
     */
    @Dict(YesOrNo.TYPE)
    private String needCaptcha;

    /**
     * 是否需要会员登录
     */
    @Dict(YesOrNo.TYPE)
    private String needLogin;

    /**
     * 提交用户唯一性限制（无限制、IP、浏览器指纹）
     */
    @NotBlank
    private String ruleLimit;

    /**
     * 备注
     */
    private String remark;
}
