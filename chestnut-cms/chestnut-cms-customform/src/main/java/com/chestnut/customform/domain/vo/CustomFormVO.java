package com.chestnut.customform.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

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

    private List<Map<String, String>> templates;

    public static CustomFormVO from(CmsCustomForm form) {
        CustomFormVO vo = new CustomFormVO();
        BeanUtils.copyProperties(form, vo, "templates");
        return vo;
    }
}
