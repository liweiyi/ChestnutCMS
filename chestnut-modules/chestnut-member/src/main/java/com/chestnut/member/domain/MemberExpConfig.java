package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员经验配置表
 */
@Getter
@Setter
@TableName(MemberExpConfig.TABLE_NAME)
public class MemberExpConfig extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_exp_config";

	@TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;
	
	/**
	 * 操作项ID
	 */
	private String opType;
	
	/**
	 * 操作项名称
	 */
	@TableField(exist = false)
	private String opTypeName;
	
	/**
	 * 积分类型
	 */
	private String levelType;
	
	/**
	 * 积分类型名称
	 */
	@TableField(exist = false)
	private String levelTypeName;
	
	/**
	 * 积分变更值
	 */
	private Integer exp;
	
	/**
	 * 每日生效次数上限
	 */
	private Integer dayLimit;
	
	/**
	 * 总生效次数上限
	 */
	private Integer totalLimit;
}
