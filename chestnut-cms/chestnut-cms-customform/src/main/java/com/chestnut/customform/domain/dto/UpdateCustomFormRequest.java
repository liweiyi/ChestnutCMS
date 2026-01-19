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
package com.chestnut.customform.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 自定义表单编辑DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class UpdateCustomFormRequest extends BaseDTO {

    @LongId
    private Long formId;

    /**
     * 名称
     */
    @NotBlank
    @Length(max = 100)
    private String name;

    /**
     * 编码
     */
    @NotBlank
    @Length(max = 50)
    private String code;

    /**
     * 是否需要验证码
     */
    @NotBlank
    @Dict(YesOrNo.TYPE)
    private String needCaptcha;

    /**
     * 是否需要会员登录
     */
    @NotBlank
    @Dict(YesOrNo.TYPE)
    private String needLogin;

    /**
     * 提交用户唯一性限制（无限制、IP、浏览器指纹）
     */
    @NotBlank
    @Length(max = 20)
    private String ruleLimit;

    /**
     * 备注
     */
    @Length(max = 500)
    private String remark;

    /**
     * 模板配置
     */
    private List<PublishPipeTemplate> templates;
}
