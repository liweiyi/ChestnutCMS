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

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.MessagePriv;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageTypeInfo;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.dto.CreateMessageConfigReq;
import com.chestnut.message.domain.dto.QueryMessageConfigReq;
import com.chestnut.message.domain.dto.TestMessageConfigReq;
import com.chestnut.message.domain.dto.UpdateMessageConfigReq;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;


import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息配置前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MSG.CONFIG_MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/config")
public class MessageConfigController extends BaseRestController {

	private final List<IMessageType<?>> messageTypes;

	private final IMessageConfigService messageConfigService;

	@XComment("{API.DOC.MSG.GET_MSG_TYPES}")
	@GetMapping("/types")
	public R<?> getMessageTypes() {
        List<MessageTypeInfo> list = messageTypes.stream().map(MessageTypeInfo::of).toList();
        return R.ok(list);
    }

    @XComment("{API.DOC.MSG.CONFIG_GET_LIST}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.CONFIG_VIEW)
	@GetMapping("/list")
	public R<TableData<CcMessageConfig>> getList(@Validated QueryMessageConfigReq req) {
		PageRequest pr = this.getPageRequest();
		Page<CcMessageConfig> page = this.messageConfigService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(req.getType()), CcMessageConfig::getType, req.getType())
				.like(StringUtils.isNotEmpty(req.getName()), CcMessageConfig::getName, req.getName())
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

    @XComment("{API.DOC.MSG.CONFIG_GET_DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.CONFIG_VIEW)
	@GetMapping("/{configId}")
	public R<CcMessageConfig> getDetail(@PathVariable @LongId @XComment("{API.DOC.MSG.CONFIG_ID}") Long configId) {
		CcMessageConfig config = this.messageConfigService.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", configId));
		return R.ok(config);
	}

    @XComment("{API.DOC.MSG.CONFIG_ADD}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.CONFIG_ADD)
	@Log(title = "新增消息配置", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@RequestBody @Validated CreateMessageConfigReq req) {
		this.messageConfigService.addConfig(req);
		return R.ok();
	}

    @XComment("{API.DOC.MSG.CONFIG_UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.CONFIG_EDIT)
	@Log(title = "编辑消息配置", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> update(@RequestBody @Validated UpdateMessageConfigReq req) {
		this.messageConfigService.updateConfig(req);
		return R.ok();
	}

    @XComment("{API.DOC.MSG.CONFIG_DELETE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.CONFIG_DELETE)
	@Log(title = "删除消息配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.MSG.CONFIG_IDS}") List<Long> configIds) {
		Assert.isTrue(IdUtils.validate(configIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("configIds"));
		this.messageConfigService.deleteConfigs(configIds);
		return R.ok();
	}

    @XComment("{API.DOC.MSG.CONFIG_TEST}")
    @Priv(type = AdminUserType.TYPE, value = { MessagePriv.CONFIG_ADD, MessagePriv.CONFIG_EDIT }, mode = SaMode.OR)
    @Log(title = "测试消息配置", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public R<Void> testSend(@RequestBody @Validated TestMessageConfigReq req) {
        this.messageConfigService.testSend(req);
        return R.ok();
    }
}
