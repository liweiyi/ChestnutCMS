package com.chestnut.system.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 定时任务配置表 sys_scheduled_task
 */
@Getter
@Setter
@TableName(SysScheduledTaskLog.TABLE_NAME)
public class SysScheduledTaskLog implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_scheduled_task_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;
	
	/**
	 * 任务ID
	 */
	private Long taskId;
	
	/**
	 * 任务类型
	 */
	private String taskType;

	/**
	 * 任务准备执行时间，进入线程池队列的时间
	 */
	private LocalDateTime readyTime;

	/**
	 * 任务开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 任务结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 任务中断时间
	 */
	private LocalDateTime interruptTime;

	/**
	 * 任务进度百分比
	 */
	private int percent;
	
	/**
	 * 执行结果（SuccessOrFail）
	 */
	private String result;
	
	/**
	 * 错误信息
	 */
	private String message;
	
	/**
	 * 日志创建时间
	 */
	private LocalDateTime logTime;
}
