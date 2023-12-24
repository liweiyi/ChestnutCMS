package com.chestnut.block;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 自定义区块页面部件内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class BlockCoreDataHandler implements ICoreDataHandler {

    private final IPageWidgetService pageWidgetService;

    @Override
    public void onSiteImport(SiteImportContext context) {
        int percent = AsyncTaskManager.getTaskProgressPercent();
        AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
                "正在处理自定义区块详情数据");
        this.pageWidgetService.lambdaQuery()
            .eq(CmsPageWidget::getSiteId, context.getSite().getSiteId())
            .eq(CmsPageWidget::getType, ManualPageWidgetType.ID)
            .list().forEach(pageWidget -> {
                if (StringUtils.isNotEmpty(pageWidget.getContent())) {
                    List<ManualPageWidgetType.RowData> rowData = JacksonUtils
                            .fromList(pageWidget.getContent(), ManualPageWidgetType.RowData.class);
                    if (Objects.nonNull(rowData)) {
                        rowData.forEach(row -> {
                            row.getItems().forEach(item -> {
                                // 处理iurl
                                item.setLogo(context.dealInternalUrl(item.getLogo()));
                                item.setUrl(context.dealInternalUrl(item.getUrl()));
                            });
                        });
                    }
                }
            });
    }
}
