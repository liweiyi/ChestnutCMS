package com.chestnut.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 定时任务配置表 sys_scheduled_task
 */
@Getter
@Setter
@TableName(SysScheduledTask.TABLE_NAME)
public class SysScheduledTask extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_scheduled_task";

	@TableId(value = "task_id", type = IdType.INPUT)
	private Long taskId;
	
	/**
	 * 任务类型
	 */
	private String taskType;
	
	/**
	 * 是否启用
	 */
	private String status;

	/**
	 * 触发器类型（ScheduledTaskTriggerType）
	 */
	private String taskTrigger;
	
	/**
	 * 触发器配置（JSON格式）
	 */
	private String triggerArgs;
}
