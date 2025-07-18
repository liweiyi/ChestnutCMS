/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.customform.domain.vo;

import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import com.chestnut.customform.domain.CmsCustomForm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 自定义表单详情VO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CustomFormVO {

    private Long formId;

    private Long siteId;

    private Long modelId;

    private String name;

    private String code;

    private Integer status;

    private String needCaptcha;

    private String needLogin;

    private String ruleLimit;

    private List<PublishPipeTemplate> templates;

    public static CustomFormVO from(CmsCustomForm form) {
        CustomFormVO vo = new CustomFormVO();
        BeanUtils.copyProperties(form, vo, "templates");
        return vo;
    }
}
