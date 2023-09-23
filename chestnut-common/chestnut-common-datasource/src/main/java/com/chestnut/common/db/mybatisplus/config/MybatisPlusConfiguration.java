package com.chestnut.common.db.mybatisplus.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.chestnut.common.utils.JacksonUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.chestnut.**.mapper")
public class MybatisPlusConfiguration {

	static {
		JacksonTypeHandler.setObjectMapper(JacksonUtils.getObjectMapper());
	}

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(this.paginationInnerInterceptor()); // 分页支持
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor()); // 防止全表更新与删除
	 	interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor()); // 乐观锁
		return interceptor;
	}

	private PaginationInnerInterceptor paginationInnerInterceptor() {
		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		paginationInnerInterceptor.setMaxLimit(-1L);
		paginationInnerInterceptor.setOverflow(true);
		return paginationInnerInterceptor;
	}

	@Bean
	public ConfigurationCustomizer customizer() {
		return configuration -> configuration.setDefaultEnumTypeHandler(MybatisEnumTypeHandler.class);
	}
}