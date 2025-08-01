/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.util;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsContentOpLog;
import com.chestnut.contentcore.service.IContentOpLogService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * ContentLogUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
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
            opLog.setCatalogId(content.getCatalogId());
            opLog.setCatalogAncestors(content.getCatalogAncestors());
            opLog.setContentId(content.getContentId());
            opLog.setType(opType);
            opLog.setDetails(details);
            opLog.setOperatorType(operatorType);
            opLog.setOperator(operator);
            opLog.setLogTime(LocalDateTime.now());
            contentOpLogService.save(opLog);
            log.debug("CC.Content[{}].log: {}", content.getContentId(), opType);
        });
    }
}
