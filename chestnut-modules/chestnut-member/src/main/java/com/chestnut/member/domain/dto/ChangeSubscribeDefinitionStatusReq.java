package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新会员订阅定义的启用状态。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.CHANGE_STATUS}")
public class ChangeSubscribeDefinitionStatusReq extends BaseDTO {

    @NotBlank
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;

    @NotNull
    @Dict(EnableOrDisable.TYPE)
    @XComment("{ENT.MEMBER_SUBSCRIPTION.STATUS}")
    private String status;
}
