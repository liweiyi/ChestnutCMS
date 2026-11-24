package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 会员订阅定义，创建与更新时间沿用基础实体的日期时间字段。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{ENT.MEMBER_SUBSCRIPTION.MEMBER_SUBSCRIPTION_DEFINITION}")
@TableName(value = MemberSubscribeDefinition.TABLE_NAME, autoResultMap = true)
public class MemberSubscribeDefinition extends BaseEntity {
    /** 数据库表名。 */
    public static final String TABLE_NAME = "cc_member_subscribe_definition";

    /** 订阅定义标识。 */
    @TableId(value = "definition_id", type = IdType.INPUT)
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
    private String definitionId;

    /** 订阅名称。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_NAME}")
    private String subscriptionName;

    /** 订阅说明。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.DESCRIPTION}")
    private String description;

    /** 当前订阅提供的业务服务项ID。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SERVICE_IDS}")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> serviceIds;

    /** 定义状态，0启用、1禁用。 */
    @XComment("{ENT.MEMBER_SUBSCRIPTION.STATUS}")
    private String status;
}
