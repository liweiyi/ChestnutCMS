package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.member.MemberSubscribeUtils;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 通过有符号秒数调整会员订阅结束时间。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.ADJUST_SUBSCRIPTION_DURATION_REQUEST}")
public class AdjustSubscribeDurationReq {

    @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Pattern(regexp = "[A-Za-z0-9_-]{1,64}", message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.REQUEST_ID}")
    private String requestId;

    @NotNull(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Min(value = 1, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_ID}")
    private Long memberId;

    @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;

    @NotNull(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Max(value = MemberSubscribeUtils.MAX_INCREASE_SECONDS, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.DELTA_SECONDS}")
    private Long deltaSeconds;

    @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @Size(max = 500, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.REASON}")
    private String reason;

    @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SOURCE_TYPE}")
    private String sourceType;

    @Size(max = 128, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SOURCE_ID}")
    private String sourceId;
}
