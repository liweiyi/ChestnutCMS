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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.mapper.CmsPageWidgetMapper;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.perms.PageWidgetPermissionType;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPermissionService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageWidgetServiceImpl extends ServiceImpl<CmsPageWidgetMapper, CmsPageWidget>
        implements IPageWidgetService {

    private static final String CACHE_KEY = CMSConfig.CachePrefix + "pagewidget:";

    private final Map<String, IPageWidgetType> pageWidgetTypes;

    private final RedisCache redisCache;

    private final ISysPermissionService permissionService;

    @Override
    public CmsPageWidget getPageWidget(Long siteId, String code) {
        return this.redisCache.getCacheObject(CACHE_KEY + code,
                () -> this.lambdaQuery().eq(CmsPageWidget::getSiteId, siteId).eq(CmsPageWidget::getCode, code).one());
    }

    @Override
    public IPageWidgetType getPageWidgetType(String type) {
        IPageWidgetType pwt = this.pageWidgetTypes.get(IPageWidgetType.BEAN_NAME_PREFIX + type);
        Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORTED_PAGE_WIDGET_TYPE.exception());
        return pwt;
    }

    @Override
    public List<IPageWidgetType> getPageWidgetTypes() {
        return this.pageWidgetTypes.values().stream().collect(Collectors.toList());
    }

    @Override
    public boolean checkCodeUnique(Long siteId, String code, Long pageWidgetId) {
        LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>().eq(CmsPageWidget::getCode, code)
                .eq(CmsPageWidget::getSiteId, siteId)
                .ne(pageWidgetId != null && pageWidgetId > 0, CmsPageWidget::getPageWidgetId, pageWidgetId)
                .last("limit 1");
        return this.count(q) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPageWidget(IPageWidget pw) {
        boolean checkCodeUnique = checkCodeUnique(pw.getPageWidgetEntity().getSiteId(),
                pw.getPageWidgetEntity().getCode(), null);
        Assert.isTrue(checkCodeUnique,
                () -> CommonErrorCode.DATA_CONFLICT.exception("code: " + pw.getPageWidgetEntity().getCode()));

        pw.add();
        // 授权给添加人
        this.permissionService.grantUserPermission(
                pw.getOperator(),
                CatalogPermissionType.ID,
                CmsPrivUtils.getAllPageWidgetPermissions(pw.getPageWidgetEntity().getPageWidgetId())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePageWidget(IPageWidget pw) {
        this.checkPermission(PageWidgetPermissionType.PageWidgetPrivItem.Edit, pw.getPageWidgetEntity().getPageWidgetId(), pw.getOperator());

        CmsPageWidget pageWidget = this.getById(pw.getPageWidgetEntity().getPageWidgetId());
        Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pagewidgetId",
                pw.getPageWidgetEntity().getPageWidgetId()));

        boolean checkCodeUnique = checkCodeUnique(pw.getPageWidgetEntity().getSiteId(),
                pw.getPageWidgetEntity().getCode(), pw.getPageWidgetEntity().getPageWidgetId());
        Assert.isTrue(checkCodeUnique,
                () -> CommonErrorCode.DATA_CONFLICT.exception("code: " + pw.getPageWidgetEntity().getCode()));

        pw.save();

        this.redisCache.deleteObject(CACHE_KEY + pageWidget.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePageWidgets(List<Long> pageWidgetIds, LoginUser operator) {
        List<CmsPageWidget> pageWidgets = this.listByIds(pageWidgetIds);
        for (CmsPageWidget pageWidget : pageWidgets) {
            this.checkPermission(PageWidgetPermissionType.PageWidgetPrivItem.Delete, pageWidget.getPageWidgetId(), operator);
            IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
            IPageWidget pw = pwt.loadPageWidget(pageWidget);
            pw.delete();
            this.redisCache.deleteObject(CACHE_KEY + pageWidget.getCode());
        }
    }

    @Override
    public void deletePageWidgetsByCatalog(CmsCatalog catalog) {
        List<CmsPageWidget> list = this.lambdaQuery().likeRight(CmsPageWidget::getCatalogAncestors, catalog.getAncestors()).list();
        for (int i = 0; i < list.size(); i++) {
            CmsPageWidget pageWidget = list.get(i);
            AsyncTaskManager.setTaskProgressInfo((i * 100) / list.size(), "正在删除页面部件：" + i + " / " + list.size());
            IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
            IPageWidget pw = pwt.loadPageWidget(pageWidget);
            pw.delete();
            this.redisCache.deleteObject(CACHE_KEY + pageWidget.getCode());
        }
    }

    @Override
    public void publishPageWidgets(List<Long> pageWidgetIds, LoginUser operator) throws TemplateException, IOException {
        List<CmsPageWidget> pageWidgets = this.listByIds(pageWidgetIds);
        for (CmsPageWidget pageWidget : pageWidgets) {
            this.checkPermission(PageWidgetPermissionType.PageWidgetPrivItem.Publish, pageWidget.getPageWidgetId(), operator);
            IPageWidgetType pwt = this.getPageWidgetType(pageWidget.getType());
            IPageWidget pw = pwt.loadPageWidget(pageWidget);
            pw.setOperator(StpAdminUtil.getLoginUser());
            pw.publish();
        }
    }

    private void checkPermission(PageWidgetPermissionType.PageWidgetPrivItem privItem, Long pageWidgetId, LoginUser loginUser) {
        PermissionUtils.checkPermission(privItem.getPermissionKey(pageWidgetId), loginUser);
    }
}
