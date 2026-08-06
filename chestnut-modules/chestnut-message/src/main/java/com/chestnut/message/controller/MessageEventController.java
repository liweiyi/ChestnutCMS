/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.MessagePriv;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageEvent;
import com.chestnut.message.domain.dto.SaveMessageEventReq;
import com.chestnut.message.domain.dto.TestEventNotifyReq;
import com.chestnut.message.monitor.MessageConfigMonitoredCache;
import com.chestnut.message.service.IMessageEventService;
import com.chestnut.system.security.AdminUserType;
import freemarker.template.TemplateException;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 消息事件前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MSG.EVENT_MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/event")
public class MessageEventController extends BaseRestController {

    private final MessageConfigMonitoredCache messageConfigCache;

    private final IMessageEventService messageEventService;

    private final AsyncTaskManager asyncTaskManager;

    @XComment("{API.DOC.MSG.EVENT_GET_LIST}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.EVENT_VIEW)
    @GetMapping("/list")
    public R<TableData<CcMessageEvent>> getList(@RequestParam(value = "query", required = false) @XComment("{CC.ENTITY.QUERY}") String query) {
        PageRequest pr = this.getPageRequest();
        Page<CcMessageEvent> page = this.messageEventService.lambdaQuery()
                .like(StringUtils.isNotEmpty(query), CcMessageEvent::getName, query)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page);
    }

    @XComment("{API.DOC.MSG.EVENT_GET_DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.EVENT_VIEW)
    @GetMapping("/{eventId}")
    public R<CcMessageEvent> getDetail(@PathVariable @NotBlank @XComment("{API.DOC.MSG.EVENT_ID}") String eventId) {
        CcMessageEvent event = this.messageEventService.getById(eventId);
        Assert.notNull(event, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("eventId", eventId));
        List<CcMessageEvent.EventNotify> notifies = event.getNotifies().stream().filter(notify -> {
            CcMessageConfig config = messageConfigCache.get(notify.getConfigId());
            if (Objects.nonNull(config)) {
                notify.setType(config.getType());
                notify.setName(config.getName());
                return true;
            }
            return false;
        }).toList();
        event.setNotifies(notifies);
        return R.ok(event);
    }

    @XComment("{API.DOC.MSG.EVENT_SAVE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.EVENT_SAVE)
    @Log(title = "新增/编辑消息事件", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    public R<Void> save(@RequestBody @Validated SaveMessageEventReq req) {
        this.messageEventService.saveEvent(req);
        return R.ok();
    }

    @XComment("{API.DOC.MSG.EVENT_DELETE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.EVENT_DELETE)
    @Log(title = "删除消息事件", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.MSG.EVENT_IDS}") List<String> ids) {
        this.messageEventService.deleteEvents(ids);
        return R.ok();
    }

    @XComment("{API.DOC.MSG.EVENT_TEST}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.EVENT_SAVE)
    @Log(title = "测试消息事件", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public R<?> test(@RequestBody @Validated TestEventNotifyReq req) throws TemplateException, IOException {
        AsyncTask task = new AsyncTask() {
            @Override
            public void run0() {
                try {
                    messageEventService.triggerEvent(req.getEventId(), req.getParams());
                } catch (Exception e) {
                    this.addErrorMessage(e.getMessage());
                }
            }
        };
        task.setTaskId("MessageEventTest_" + req.getEventId());
        task.setLocale(LocaleContextHolder.getLocale());
        asyncTaskManager.execute(task);
        return R.ok(task.getTaskId());
    }
}
