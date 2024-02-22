/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.customform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.domain.dto.CustomFormAddDTO;
import com.chestnut.customform.domain.dto.CustomFormEditDTO;
import com.chestnut.customform.domain.vo.CustomFormVO;
import com.chestnut.customform.permission.CustomFormPriv;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自定义表单控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/customform")
@RequiredArgsConstructor
public class CustomFormController extends BaseRestController {

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ICustomFormService customFormService;

    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.View)
    @GetMapping
    public R<?> getList(@RequestParam(value = "query", required = false) String query,
                        @RequestParam(required = false) String status) {
        PageRequest pr = this.getPageRequest();
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        Page<CmsCustomForm> page = this.customFormService.lambdaQuery()
                .eq(CmsCustomForm::getSiteId, site.getSiteId())
                .eq(StringUtils.isNotEmpty(status), CmsCustomForm::getStatus, status)
                .like(StringUtils.isNotEmpty(query), CmsCustomForm::getName, query)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page);
    }

    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.View)
    @GetMapping("/{formId}")
    public R<?> getDetail(@PathVariable @LongId Long formId) {
        CmsCustomForm form = this.customFormService.getById(formId);
        Assert.notNull(form, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("formId", formId));

        CustomFormVO vo = CustomFormVO.from(form);
        List<Map<String, String>> templates = this.publishPipeService.getPublishPipes(form.getSiteId())
                .stream().map(pp -> Map.of(
                    "name", pp.getName(),
                    "code", pp.getCode(),
                    "template", form.getTemplates().getOrDefault(pp.getCode(), "")
        )).toList();
        vo.setTemplates(templates);
        return R.ok(vo);
    }

    @Log(title = "新增自定义表单", businessType = BusinessType.INSERT)
    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.Add)
    @PostMapping
    public R<?> add(@RequestBody @Validated CustomFormAddDTO dto) {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        dto.setOperator(StpAdminUtil.getLoginUser());
        dto.setSiteId(site.getSiteId());
        this.customFormService.addCustomForm(dto);
        return R.ok();
    }

    @Log(title = "编辑自定义表单", businessType = BusinessType.UPDATE)
    @Priv(type = AdminUserType.TYPE, value = {CustomFormPriv.Add, CustomFormPriv.Edit})
    @PutMapping
    public R<?> edit(@RequestBody @Validated CustomFormEditDTO dto) {
        dto.setOperator(StpAdminUtil.getLoginUser());
        this.customFormService.editCustomForm(dto);
        return R.ok();
    }

    @Log(title = "删除自定义表单", businessType = BusinessType.DELETE)
    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.Delete)
    @DeleteMapping
    public R<?> remove(@RequestBody @Validated @NotEmpty List<Long> formIds) {
        this.customFormService.deleteCustomForm(formIds);
        return R.ok();
    }

    @Log(title = "发布自定义表单", businessType = BusinessType.UPDATE)
    @Priv(type = AdminUserType.TYPE, value = { CustomFormPriv.Add, CustomFormPriv.Edit })
    @PutMapping("/publish")
    public R<?> publish(@RequestBody @Validated @NotEmpty List<Long> formIds) {
        this.customFormService.publishCustomForms(formIds, StpAdminUtil.getLoginUser().getUsername());
        return R.ok();
    }

    @Log(title = " 下线自定义表单", businessType = BusinessType.UPDATE)
    @Priv(type = AdminUserType.TYPE, value = { CustomFormPriv.Add, CustomFormPriv.Edit })
    @PutMapping("/offline")
    public R<?> offline(@RequestBody @Validated @NotEmpty List<Long> formIds) throws IOException {
        this.customFormService.offlineCustomForms(formIds, StpAdminUtil.getLoginUser().getUsername());
        return R.ok();
    }
}
