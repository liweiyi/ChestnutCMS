package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * 创建或更新会员订阅服务项，内置项名称由系统维护。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.SAVE_REQUEST}")
public class SaveMemberSubscribeServiceReq extends BaseDTO {

    @NotBlank(message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.ID}")
    @Length(max = 100, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.ID}")
    @Pattern(regexp = RegexConsts.REGEX_CODE, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.ID}")
    @XComment("{ENT.MEMBER_SUBSCRIBE_SERVICE.ID}")
    private String serviceId;

    @Length(max = 100, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.NAME}")
    @XComment("{ENT.MEMBER_SUBSCRIBE_SERVICE.NAME}")
    private String name;

    @Length(max = 400, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.REMARK}")
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
}
