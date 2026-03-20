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

import com.chestnut.common.annotation.XComment;
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
@XComment("{API.DOC.CMS.CUSTOM_FORM.UPDATE_REQ}")
public class UpdateCustomFormRequest extends BaseDTO {

    @LongId
    @XComment("{API.DOC.CMS.CUSTOM_FORM.FORM_ID}")
    private Long formId;

    @NotBlank
    @Length(max = 100)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.FORM_NAME}")
    private String name;

    @NotBlank
    @Length(max = 50)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.FORM_CODE}")
    private String code;

    @NotBlank
    @Dict(YesOrNo.TYPE)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.NEED_CAPTCHA}")
    private String needCaptcha;

    @NotBlank
    @Dict(YesOrNo.TYPE)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.NEED_LOGIN}")
    private String needLogin;

    @NotBlank
    @Length(max = 20)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.RULE_LIMIT}")
    private String ruleLimit;

    @Length(max = 500)
    @XComment("{API.DOC.CMS.CUSTOM_FORM.REMARK}")
    private String remark;

    @XComment("{API.DOC.CMS.CUSTOM_FORM.TEMPLATES}")
    private List<PublishPipeTemplate> templates;
}
