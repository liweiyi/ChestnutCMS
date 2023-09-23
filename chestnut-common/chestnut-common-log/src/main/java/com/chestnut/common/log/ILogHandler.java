package com.chestnut.common.log;

/**
 * 日志处理器
 */
public interface ILogHandler {
	
	/**
	 * 是否处理指定日志类型
	 * 
	 * @param logType
	 * @return
	 */
	public boolean test(String logType);

	/**
	 * 处理日志
	 * 
	 * @param logDetail
	 */
	public void handler(LogDetail logDetail);
}
