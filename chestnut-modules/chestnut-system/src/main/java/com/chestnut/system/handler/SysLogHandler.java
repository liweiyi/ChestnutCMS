package com.chestnut.system.handler;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.log.ILogHandler;
import com.chestnut.common.log.LogDetail;
import com.chestnut.common.log.restful.RestfulLogData;
import com.chestnut.common.log.restful.RestfulLogType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysOperLog;
import com.chestnut.system.service.ISysOperLogService;

import lombok.RequiredArgsConstructor;

/**
 * 系统操作日志处理器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class SysLogHandler implements ILogHandler {

	protected Logger logger = LoggerFactory.getLogger(SysLogHandler.class);

	private final AsyncTaskManager asyncTaskManager;

	private final ISysOperLogService operLogService;

	@Override
	public boolean test(String logType) {
		return RestfulLogType.TYPE.equals(logType);
	}

	@Override
	public void handler(LogDetail logDetail) {
		// 操作日志
		this.processOperLog(logDetail);
	}

	private void processOperLog(LogDetail logDetail) {
		Map<String, Object> details = logDetail.getDetails();
		String requestMethod = MapUtils.getString(details, RestfulLogData.PARAM_REQUEST_METHOD);
		try {
			SysOperLog operLog = new SysOperLog();
			operLog.setTitle(logDetail.getLogTitle());
			operLog.setBusinessType(logDetail.getBusinessType());
			operLog.setOperIp(MapUtils.getString(details, RestfulLogData.PARAM_IP));
			operLog.setOperUrl(MapUtils.getString(details, RestfulLogData.PARAM_REQUEST_URI));
			operLog.setOperatorType(MapUtils.getString(details, RestfulLogData.PARAM_USER_TYPE));
			operLog.setOperUid(MapUtils.getLong(details, RestfulLogData.PARAM_USER_ID));
			operLog.setOperName(MapUtils.getString(details, RestfulLogData.PARAM_ACCOUNT));
			operLog.setUserAgent(MapUtils.getString(details, RestfulLogData.PARAM_USER_AGENT));
			operLog.setResponseCode(MapUtils.getInteger(details, RestfulLogData.PARAM_RESPONSE_CODE));
			String responseResult = MapUtils.getString(details, RestfulLogData.PARAM_RESPONSE_RESULT);
			operLog.setResponseResult(StringUtils.substring(responseResult, 0, 2000));
			String requestArgs = MapUtils.getString(details, RestfulLogData.PARAM_REQUEST_ARGS);
			operLog.setRequestArgs(StringUtils.substring(requestArgs, 0, 500));
			// 设置方法名称
			String requestFunction = MapUtils.getString(details, RestfulLogData.PARAM_REQUEST_FUNCTION);
			operLog.setMethod(requestFunction);
			// 设置请求方式
			operLog.setRequestMethod(requestMethod);
			operLog.setOperTime(logDetail.getLogTime());
			operLog.setCost(logDetail.getCost());
			// 保存数据库
			asyncTaskManager.execute(operLogService.recordOper(operLog));
		} catch (Exception exp) {
			// 记录本地异常日志
			logger.error("异常信息:{}", exp.getMessage());
			exp.printStackTrace();
		}
	}
}
