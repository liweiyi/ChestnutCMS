package com.chestnut.system.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledTaskDTO extends BaseDTO {
	
	private Long taskId;
	
	private String taskType;
	
	private String taskTrigger;
	
	private String cron;
	
	private String fixedRate;
	
	private Long seconds;
	
	private Long delaySeconds;
	
	private String status;
	
	private String remark;
}

