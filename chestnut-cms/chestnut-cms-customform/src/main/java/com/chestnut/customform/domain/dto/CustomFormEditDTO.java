package com.chestnut.customform.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 自定义表单编辑DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CustomFormEditDTO extends BaseDTO {

    @LongId
    private Long formId;

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
     * 唯一性限制下的每日提交次数上限（0表示不限制）
     */
    private Integer dayLimit;

    /**
     * 模板配置
     */
    private List<Map<String, String>> templates;

    /**
     * 备注
     */
    private String remark;
}
