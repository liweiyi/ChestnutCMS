package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 会员订阅记录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_SUBSCRIPTION_RECORD}")
@TableName(MemberSubscribeRecord.TABLE_NAME)
public class MemberSubscribeRecord implements Serializable {

    public static final String TABLE_NAME = "cc_member_subscribe";

    /** 会员订阅记录主键ID。 */
    @TableId(value = "record_id", type = IdType.INPUT)
    @XComment("{ENT.MEMBER_SUBSCRIPTION.RECORD_ID}")
    private String recordId;

    /** 关联会员ID。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_ID}")
    private Long memberId;

    /** 关联订阅定义ID。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;

    /** 订阅结束时间，UTC纪元秒。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.EXPIRE_TIME}")
    private Long expireTime;

    /** 首次关联时间，UTC纪元毫秒。 */
    @XComment("{CC.ENTITY.CREATE_TIME}")
    private Long createTime;
}
