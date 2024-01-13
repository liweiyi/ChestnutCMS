package com.chestnut.seo.job;

import com.chestnut.seo.service.BaiduSitemapService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 站点地图定时更新任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + SitemapJobHandler.JOB_NAME)
public class SitemapJobHandler extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "SiteMapJobHandler";

	private final BaiduSitemapService bdSitemapService;

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
		IScheduledHandler.logger.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		this.bdSitemapService.generateSitemapXml();
		IScheduledHandler.logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
