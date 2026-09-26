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
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
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
import com.chestnut.message.core.IMessagePusher;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.CreateMessageTemplateReq;
import com.chestnut.message.domain.dto.QueryMessageTemplateReq;
import com.chestnut.message.domain.dto.UpdateMessageTemplateReq;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageTemplateService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.PublicImageFileService;
import com.chestnut.system.validator.LongId;


import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 消息模板前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MSG.TEMPLATE_MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/template")
public class MessageTemplateController extends BaseRestController {

    private final IMessageTemplateService messageTemplateService;

    private final PublicImageFileService publicImageFileService;

    @XComment("{API.DOC.MSG.TEMPLATE_UPLOAD_IMAGE}")
    @Priv(type = AdminUserType.TYPE, value = {MessagePriv.TEMPLATE_ADD, MessagePriv.TEMPLATE_EDIT})
    @PostMapping("/image/upload")
    public R<Map<String, String>> uploadImage(
            @RequestParam("file") @XComment("{API.DOC.MSG.TEMPLATE_IMAGE_FILE}") MultipartFile file) throws IOException {
        return R.ok(Map.of("fileName", publicImageFileService.upload(file, "message/template")));
    }

    @XComment("{API.DOC.MSG.TEMPLATE_GET_LIST}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.TEMPLATE_VIEW)
	@GetMapping("/list")
	public R<TableData<CcMessageTemplate>> getList(@Validated QueryMessageTemplateReq req) {
		PageRequest pr = this.getPageRequest();
		Page<CcMessageTemplate> page = this.messageTemplateService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(req.getType()), CcMessageTemplate::getType, req.getType())
				.like(StringUtils.isNotEmpty(req.getName()), CcMessageTemplate::getName, req.getName())
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

    @XComment("{API.DOC.MSG.TEMPLATE_GET_DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.TEMPLATE_VIEW)
	@GetMapping("/{templateId}")
	public R<CcMessageTemplate> getDetail(@PathVariable @LongId @XComment("{API.DOC.MSG.TEMPLATE_ID}") Long templateId) {
        CcMessageTemplate template = this.messageTemplateService.getById(templateId);
		Assert.notNull(template, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", templateId));
		return R.ok(template);
	}

    @XComment("{API.DOC.MSG.TEMPLATE_ADD}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.TEMPLATE_ADD)
	@Log(title = "新增消息推送配置", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@RequestBody @Validated CreateMessageTemplateReq req) {
		this.messageTemplateService.addTemplate(req);
		return R.ok();
	}

    @XComment("{API.DOC.MSG.TEMPLATE_UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.TEMPLATE_EDIT)
	@Log(title = "编辑消息推送配置", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> update(@RequestBody @Validated UpdateMessageTemplateReq req) {
		this.messageTemplateService.updateTemplate(req);
		return R.ok();
	}

    @XComment("{API.DOC.MSG.TEMPLATE_DELETE}")
    @Priv(type = AdminUserType.TYPE, value = MessagePriv.TEMPLATE_DELETE)
	@Log(title = "删除消息推送配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.MSG.TEMPLATE_IDS}") List<Long> ids) {
		Assert.isTrue(IdUtils.validate(ids), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("ids"));
		this.messageTemplateService.deleteTemplates(ids);
		return R.ok();
	}
}
