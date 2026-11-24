package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新会员订阅定义及服务范围。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.UPDATE_SUBSCRIPTION_DEFINITION_REQUEST}")
public class UpdateSubscribeDefinitionReq extends CreateSubscribeDefinitionReq {

    @NotBlank
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;
}
