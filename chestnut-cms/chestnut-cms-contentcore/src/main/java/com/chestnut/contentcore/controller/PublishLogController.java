package com.chestnut.contentcore.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.contentcore.config.CMSPublishConfig;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 发布日志管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/publish/")
public class PublishLogController extends BaseRestController {

	private final StringRedisTemplate redisTemplate;

	/**
	 * 发布队列任务数量
	 */
	@GetMapping("/taskCount")
	public R<?> getPublishTaskCount() {
		StreamInfo.XInfoStream info = redisTemplate.opsForStream().info(CMSPublishConfig.PublishStreamName);
		return R.ok(info.streamLength());
	}

	/**
	 * 清理发布队列
	 */
	@DeleteMapping("/clear")
	public R<?> clearPublishTask() {
		redisTemplate.delete(CMSPublishConfig.PublishStreamName);
		try {
			redisTemplate.opsForStream().createGroup(CMSPublishConfig.PublishStreamName, CMSPublishConfig.PublishConsumerGroup);
		} catch (Exception ignored) {
		}
		return R.ok();
	}
}
