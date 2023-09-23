package com.chestnut.system.mapper;

import org.apache.ibatis.annotations.Delete;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.system.domain.SysLogininfor;

/**
 * 系统访问日志情况信息 数据层
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {
	
	/**
	 * 清空系统登录日志
	 * 
	 * @return 结果
	 */
	@Delete("truncate table " + SysLogininfor.TABLE_NAME)
	public int cleanLogininfor();
}
