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
package com.chestnut.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发布点管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/publishpipe")
@RequiredArgsConstructor
public class PublishPipeController extends BaseRestController {

    private final IPublishPipeService publishPipeService;

    private final ISiteService siteService;

    /**
     * 获取当前站点发布通道选择器数据
     *
     * @return
     */
    @Priv(type = AdminUserType.TYPE, value = CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER)
    @GetMapping("/selectData")
    public R<?> bindSelectData() {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        List<PublishPipeProp> datalist = this.publishPipeService.getPublishPipes(site.getSiteId())
                .stream().map(p -> PublishPipeProp.newInstance(p.getCode(), p.getName(), null))
                .collect(Collectors.toList());
        return this.bindDataTable(datalist);
    }

    /**
     * 查询当前站点发布通道数据列表
     *
     * @return
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @GetMapping("/list")
    public R<?> list() {
        PageRequest pr = this.getPageRequest();
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        Page<CmsPublishPipe> page = publishPipeService.page(new Page<CmsPublishPipe>(pr.getPageNumber(), pr.getPageSize(), true)
                , new LambdaQueryWrapper<CmsPublishPipe>().eq(CmsPublishPipe::getSiteId, site.getSiteId()).orderByAsc(CmsPublishPipe::getSort));
        return this.bindDataTable(page);
    }

    /**
     * 获取发布通道详情
     *
     * @param publishPipeId 发布通道ID
     * @return
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @GetMapping(value = "/{publishPipeId}")
    public R<?> getInfo(@PathVariable @LongId Long publishPipeId) {
        CmsPublishPipe publishPipe = publishPipeService.getById(publishPipeId);
        Assert.notNull(publishPipe, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("publishPipeId", publishPipeId));
        return R.ok(publishPipe);
    }

    /**
     * 新增发布通道数据
     *
     * @param publishPipe
     * @return
     * @throws IOException
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @Log(title = "新增发布通道", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> addSave(@RequestBody @Validated CmsPublishPipe publishPipe) throws IOException {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        publishPipe.setSiteId(site.getSiteId());
        publishPipe.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.addPublishPipe(publishPipe);
        return R.ok();
    }

    /**
     * 修改发布通道数据
     *
     * @param publishPipe
     * @return
     * @throws IOException
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @Log(title = "编辑发布通道", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> editSave(@RequestBody @Validated CmsPublishPipe publishPipe) throws IOException {
        publishPipe.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.savePublishPipe(publishPipe);
        return R.ok();
    }

    /**
     * 删除发布通道数据
     *
     * @param publishPipeIds 发布通道IDs
     * @return
     * @throws IOException
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @Log(title = "删除发布通道", businessType = BusinessType.DELETE)
    @DeleteMapping
    public R<String> remove(@RequestBody @NotEmpty List<Long> publishPipeIds) throws IOException {
        this.publishPipeService.deletePublishPipe(publishPipeIds);
        return R.ok();
    }

    /**
     * 启用发布通道
     *
     * @param publishPipeId 发布通道ID
     * @return
     * @throws IOException
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @Log(title = "启用发布通道", businessType = BusinessType.UPDATE)
    @PostMapping("/enable/{publishPipeId}")
    public R<String> enable(@PathVariable("publishPipeId") @LongId Long publishPipeId) throws IOException {
        CmsPublishPipe publishPipe = this.publishPipeService.getById(publishPipeId);
        Assert.notNull(publishPipe, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("publishPipeId", publishPipe));

        publishPipe.setState(EnableOrDisable.ENABLE);
        publishPipe.updateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.savePublishPipe(publishPipe);
        return R.ok();
    }

    /**
     * 禁用发布通道
     *
     * @param publishPipeId 发布通道ID
     * @return
     * @throws IOException
     */
    @Priv(
            type = AdminUserType.TYPE,
            value = { ContentCorePriv.PublishPipeView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
    @Log(title = "禁用发布通道", businessType = BusinessType.UPDATE)
    @PostMapping("/disable/{publishPipeId}")
    public R<String> disable(@PathVariable("publishPipeId") Long publishPipeId) throws IOException {
        CmsPublishPipe publishPipe = this.publishPipeService.getById(publishPipeId);
        if (publishPipe == null) {
            return R.fail("数据ID错误：" + publishPipeId);
        }
        publishPipe.setState(EnableOrDisable.DISABLE);
        publishPipe.updateBy(StpAdminUtil.getLoginUser().getUsername());
        this.publishPipeService.savePublishPipe(publishPipe);
        return R.ok();
    }
}
