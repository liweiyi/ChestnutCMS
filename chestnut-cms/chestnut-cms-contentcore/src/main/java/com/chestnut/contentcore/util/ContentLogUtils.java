package com.chestnut.contentcore.util;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsContentOpLog;
import com.chestnut.contentcore.service.IContentOpLogService;

import java.time.LocalDateTime;

/**
 * ContentLogUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ContentLogUtils {

    private static final IContentOpLogService contentOpLogService = SpringUtils.getBean(IContentOpLogService.class);

    private static final AsyncTaskManager asyncTaskManager = SpringUtils.getBean(AsyncTaskManager.class);

    public static void addLog(String opType, CmsContent content, LoginUser operator) {
        addLog(opType, content, null, operator.getUserType(), operator.getUsername());
    }

    public static void addLog(String opType, CmsContent content, String operatorType, String operator) {
        addLog(opType, content, null, operatorType, operator);
    }

    public static void addLog(String opType, CmsContent content, String details, String operatorType, String operator) {
        asyncTaskManager.execute(() -> {
            CmsContentOpLog opLog = new CmsContentOpLog();
            opLog.setLogId(IdUtils.getSnowflakeId());
            opLog.setSiteId(content.getSiteId());
            opLog.setContentId(content.getContentId());
            opLog.setType(opType);
            opLog.setDetails(details);
            opLog.setOperatorType(operatorType);
            opLog.setOperator(operator);
            opLog.setLogTime(LocalDateTime.now());
            contentOpLogService.save(opLog);
        });
    }
}
