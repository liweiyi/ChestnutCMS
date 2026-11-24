package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询会员订阅定义。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.QUERY_SUBSCRIPTION_DEFINITION_REQUEST}")
public class QuerySubscribeDefinitionReq {
    @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_KEY}")
    private String subscriptionKey;

    @Size(max = 128, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_NAME}")
    private String subscriptionName;

}
