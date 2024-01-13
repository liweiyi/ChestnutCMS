package com.chestnut.advertisement;

import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 广告页面部件内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class AdCoreDataHandler implements ICoreDataHandler {

    private final IAdvertisementService advertisementService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_advertisement
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出广告数据");
        List<CmsAdvertisement> list = advertisementService.lambdaQuery()
                .eq(CmsAdvertisement::getSiteId, context.getSite().getSiteId())
                .list();
        context.saveData(CmsAdvertisement.TABLE_NAME, JacksonUtils.to(list));
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入广告数据");
        List<File> files = context.readDataFiles(CmsAdvertisement.TABLE_NAME);
        files.forEach(f -> {
            List<CmsAdvertisement> list = JacksonUtils.fromList(f, CmsAdvertisement.class);
            for (CmsAdvertisement data : list) {
                try {
                    data.setAdvertisementId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setAdSpaceId(context.getPageWidgetIdMap().get(data.getAdSpaceId()));
                    data.createBy(context.getOperator());
                    data.setResourcePath(context.dealInternalUrl(data.getResourcePath()));
                    data.setRedirectUrl(context.dealInternalUrl(data.getRedirectUrl()));
                    advertisementService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入广告数据失败：" + data.getName()
                            + "[" + data.getAdvertisementId() + "]");
                    e.printStackTrace();
                }
            }
        });
    }
}
