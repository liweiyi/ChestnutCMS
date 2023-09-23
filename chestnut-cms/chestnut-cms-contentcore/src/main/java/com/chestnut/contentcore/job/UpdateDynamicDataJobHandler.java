package com.chestnut.contentcore.job;

import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时发布任务<br/>
 * 
 * 仅发布待发布状态且发布时间为空或发布时间在当前时间之前的内容
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + UpdateDynamicDataJobHandler.JOB_NAME)
public class UpdateDynamicDataJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "UpdateDynamicDataJobHandler";

	private final ContentDynamicDataService contentDynamicDataService;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		logger.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		this.contentDynamicDataService.saveDynamicDataToDB();
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
