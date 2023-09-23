package com.chestnut.system.groovy;

import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;

import com.chestnut.common.redis.RedisCache;

public class TestGroovyScript extends BaseGroovyScript {

	@Autowired
	private RedisCache redisCache;
	
	@Override
	protected void run(PrintWriter out) {
		out.println(redisCache.hasKey("adv:stat-view:2023040414"));
	}
}