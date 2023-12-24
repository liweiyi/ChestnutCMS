package com.chestnut.media;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IAudioService;
import com.chestnut.media.service.IVideoService;
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
public class MediaCoreDataHandler implements ICoreDataHandler {

    private final IAudioService audioService;

    private final IVideoService videoService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_audio
        int percent = AsyncTaskManager.getTaskProgressPercent();
        AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
                "正在导出音频内容数据");
        int pageSize = 200;
        long offset = 0;
        int fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsAudio> q = new LambdaQueryWrapper<CmsAudio>()
                    .eq(CmsAudio::getSiteId, context.getSite().getSiteId())
                    .gt(CmsAudio::getAudioId, offset)
                    .orderByAsc(CmsAudio::getAudioId);
            Page<CmsAudio> page = audioService.page(new Page<>(1, pageSize, false), q);
            if (page.getRecords().size() > 0) {
                context.saveData(CmsAudio.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
        // cms_video
        percent = AsyncTaskManager.getTaskProgressPercent();
        AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
                "正在导出视频频内容数据");
        offset = 0;
        fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>()
                    .eq(CmsVideo::getSiteId, context.getSite().getSiteId())
                    .gt(CmsVideo::getVideoId, offset)
                    .orderByAsc(CmsVideo::getVideoId);
            Page<CmsVideo> page = videoService.page(new Page<>(1, pageSize, false), q);
            if (page.getRecords().size() > 0) {
                context.saveData(CmsVideo.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        // cms_audio
        int percent = AsyncTaskManager.getTaskProgressPercent();
        AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
                "正在导入音频内容数据");
        List<File> files = context.readDataFiles(CmsAudio.TABLE_NAME);
        files.forEach(f -> {
            List<CmsAudio> list = JacksonUtils.fromList(f, CmsAudio.class);
            for (CmsAudio data : list) {
                try {
                    data.setAudioId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setContentId(context.getContentIdMap().get(data.getContentId()));
                    data.createBy(context.getOperator());
                    data.setPath(context.dealInternalUrl(data.getPath()));
                    audioService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入音频内容数据失败：" + data.getTitle()
                            + "[" + data.getAudioId() + "]");
                    e.printStackTrace();
                }
            }
        });
        // cms_video
        percent = AsyncTaskManager.getTaskProgressPercent();
        AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
                "正在导入视频内容数据");
        files = context.readDataFiles(CmsVideo.TABLE_NAME);
        files.forEach(f -> {
            List<CmsVideo> list = JacksonUtils.fromList(f, CmsVideo.class);
            for (CmsVideo data : list) {
                try {
                    data.setVideoId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setContentId(context.getContentIdMap().get(data.getContentId()));
                    data.createBy(context.getOperator());
                    data.setPath(context.dealInternalUrl(data.getPath()));
                    videoService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入视频内容数据失败：" + data.getTitle()
                            + "[" + data.getVideoId() + "]");
                    e.printStackTrace();
                }
            }
        });
    }
}
