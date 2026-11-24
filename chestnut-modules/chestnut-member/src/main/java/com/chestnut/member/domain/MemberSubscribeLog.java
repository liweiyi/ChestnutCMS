package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;

import lombok.Getter;
import lombok.Setter;

/**
 * 会员订阅时长调整审计日志
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_SUBSCRIPTION_DURATION_LOG}")
@TableName(MemberSubscribeLog.TABLE_NAME)
public class MemberSubscribeLog {

    public static final String TABLE_NAME = "cc_member_subscribe_log";

    /** 时长调整日志标识。 */
    @TableId(value = "log_id", type = IdType.INPUT)
    @XComment("{ENT.MEMBER_SUBSCRIPTION.LOG_ID}")
    private String logId;

    /** 会员订阅记录标识。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.RECORD_ID}")
    private String recordId;

    /** 会员标识。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_ID}")
    private Long memberId;

    /** 订阅定义标识。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String subscriptionId;

    /** 带符号的调整秒数，允许负数。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.DELTA_SECONDS}")
    private Long deltaSeconds;

    /** 调整前结束时间，UTC纪元秒，首次关联为0。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.BEFORE_EXPIRE_TIME}")
    private Long beforeExpireTime;

    /** 调整后结束时间，UTC纪元秒。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.AFTER_EXPIRE_TIME}")
    private Long afterExpireTime;

    /** 操作时间，UTC纪元毫秒。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.OPERATION_TIME}")
    private Long operationTime;

    /** 操作人类型。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.OPERATOR_TYPE}")
    private String operatorType;

    /** 操作人标识。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.OPERATOR_ID}")
    private String operatorId;

    /** 调整来源类型。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SOURCE_TYPE}")
    private String sourceType;

    /** 来源业务单据标识。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SOURCE_ID}")
    private String sourceId;

    /** 本次调整原因。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.REASON}")
    private String reason;
}
