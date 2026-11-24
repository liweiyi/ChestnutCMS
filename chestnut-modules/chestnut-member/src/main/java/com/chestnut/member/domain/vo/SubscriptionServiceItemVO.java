package com.chestnut.member.domain.vo;

import com.chestnut.common.annotation.XComment;

/**
 * 会员订阅定义及服务项标识。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.SUBSCRIPTION_SERVICE_ITEM}")
public record SubscriptionServiceItemVO(
        String id,
        String name,
        String description
) {
}
