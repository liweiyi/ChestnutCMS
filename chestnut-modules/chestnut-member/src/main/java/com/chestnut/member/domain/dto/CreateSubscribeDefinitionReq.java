package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 新增会员订阅定义。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.CREATE_SUBSCRIPTION_DEFINITION_REQUEST}")
public class CreateSubscribeDefinitionReq extends BaseDTO {

    /** 兼容旧客户端的订阅编码，当前定义由系统生成主键，不再持久化此字段。 */
    @Length(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Pattern(regexp = RegexConsts.REGEX_CODE, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_KEY}")
    private String subscriptionKey;

    @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Size(max = 128, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_NAME}")
    private String subscriptionName;

    @Size(max = 1000, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.DESCRIPTION}")
    private String description;

    @NotNull(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Size(max = 100, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SERVICE_IDS}")
    private List<String> serviceIds;

    @Length(max = 500)
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
}
