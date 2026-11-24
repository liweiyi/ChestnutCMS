package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询订阅时长调整审计。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.QUERY_SUBSCRIPTION_DURATION_LOG_REQUEST}")
public class QuerySubscribeDurationLogReq {
    @Min(value = 1, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_ID}")
    private Long memberId;

    @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;
}
