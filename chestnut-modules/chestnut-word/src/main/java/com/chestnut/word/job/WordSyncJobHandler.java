/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.word.job;

import com.chestnut.system.schedule.IScheduledHandler;
import com.chestnut.word.service.IErrorProneWordService;
import com.chestnut.word.service.ISensitiveWordService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 敏感词/热词数据同步任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + WordSyncJobHandler.JOB_NAME)
public class WordSyncJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "WordSyncJobHandler";
	
	private final ISensitiveWordService sensitiveWordService;

	private final IErrorProneWordService errorProneWordService;

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
		sensitiveWordService.sync();
		errorProneWordService.sync();
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
