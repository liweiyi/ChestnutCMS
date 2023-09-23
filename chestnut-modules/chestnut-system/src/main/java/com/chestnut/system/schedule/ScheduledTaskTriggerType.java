package com.chestnut.system.schedule;

import lombok.Getter;
import lombok.Setter;

public enum ScheduledTaskTriggerType {

	Cron, Periodic;
	
	public static boolean isCron(String trigger) {
		return Cron.name().equalsIgnoreCase(trigger);
	}
	
	public static boolean isPeriodic(String trigger) {
		return Periodic.name().equalsIgnoreCase(trigger);
	}
	
	@Getter
	@Setter
	public static class CronTriggerArgs {

		private String cron;
	}

	@Getter
	@Setter
	public static class PeriodicTriggerArgs {

		/**
		 * 是否固定间隔执行
		 */
		private Boolean fixedRate = false;

		/**
		 * 执行周期 fixedRate = true: 两次任务开始时间之间间隔指定时长 fixedRate = false:
		 * 上一次任务的结束时间与下一次任务开始时间间隔指定时长
		 */
		private Long seconds;

		/**
		 * 延时delay秒后开始执行
		 */
		private Long delaySeconds;
	}
}