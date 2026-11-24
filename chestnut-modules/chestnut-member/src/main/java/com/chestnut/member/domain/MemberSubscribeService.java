package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员订阅服务项
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{ENT.MEMBER_SUBSCRIBE_SERVICE.MODULE}")
@TableName(MemberSubscribeService.TABLE_NAME)
public class MemberSubscribeService extends BaseEntity {

    public static final String TABLE_NAME = "cc_member_subscribe_service";

    @TableId(value = "service_id", type = IdType.INPUT)
    @XComment("{ENT.MEMBER_SUBSCRIBE_SERVICE.ID}")
    private String serviceId;

    /** 订阅服务名称。 */
    @XComment("{ENT.MEMBER_SUBSCRIBE_SERVICE.NAME}")
    private String name;
}
