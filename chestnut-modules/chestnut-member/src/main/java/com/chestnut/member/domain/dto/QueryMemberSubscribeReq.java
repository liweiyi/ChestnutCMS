package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询会员订阅记录。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.QUERY_MEMBER_SUBSCRIPTION_REQUEST}")
public class QueryMemberSubscribeReq {
    @Min(value = 1, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_ID}")
    private Long memberId;

    @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;

    @XComment("{ENT.MEMBER_SUBSCRIPTION.VALID}")
    @com.chestnut.system.validator.Dict(com.chestnut.system.fixed.dict.YesOrNo.TYPE)
    private String valid;
}
