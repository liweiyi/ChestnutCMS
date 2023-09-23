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
