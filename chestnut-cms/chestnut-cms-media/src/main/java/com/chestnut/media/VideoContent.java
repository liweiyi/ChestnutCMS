package com.chestnut.media;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IVideoService;

public class VideoContent extends AbstractContent<List<CmsVideo>> {

	/**
	 * 视频类型：第三方分享
	 */
	public static final String TYPE_SHARE = "SHARE";

	private IVideoService videoService;

	@Override
	public String getContentType() {
		return VideoContentType.ID;
	}

	@Override
	public Long add() {
		super.add();
		this.getContentService().save(this.getContentEntity());

		if (!this.hasExtendEntity()) {
			return this.getContentEntity().getContentId();
		}

		List<CmsVideo> videoList = this.getExtendEntity();
		if (StringUtils.isNotEmpty(videoList)) {
			for (int i = 0; i < videoList.size(); i++) {
				CmsVideo video = videoList.get(i);
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(this.getContentEntity().getContentId());
				video.setSiteId(this.getContentEntity().getSiteId());
				video.setSiteId(this.getSiteId());
				video.setSortFlag(i);
				video.createBy(this.getOperator().getUsername());
				if (!TYPE_SHARE.equals(video.getType())) {
					video.setType(FileExUtils.getExtension(video.getPath()).toUpperCase());
					this.getVideoService().progressVideoInfo(video);
				}
			}
			this.getVideoService().saveBatch(videoList);
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public Long save() {
		super.save();
		this.getContentService().updateById(this.getContentEntity());
		// 链接或映射内容直接删除所有视频数据
		if (!this.hasExtendEntity()) {
			this.getVideoService().remove(new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getContentId,
					this.getContentEntity().getContentId()));
			return this.getContentEntity().getContentId();
		}
		// 视频数处理
		List<CmsVideo> videoList = this.getExtendEntity();
		// 先删除视频
		List<Long> updateVideoIds = videoList.stream().filter(video -> IdUtils.validate(video.getVideoId()))
				.map(CmsVideo::getVideoId).toList();
		this.getVideoService()
				.remove(new LambdaQueryWrapper<CmsVideo>()
						.eq(CmsVideo::getContentId, this.getContentEntity().getContentId())
						.notIn(updateVideoIds.size() > 0, CmsVideo::getVideoId, updateVideoIds));
		// 查找剩余需要修改的视频
		Map<Long, CmsVideo> oldVideoMap = this.getVideoService().lambdaQuery()
				.eq(CmsVideo::getContentId, this.getContentEntity().getContentId()).list().stream()
				.collect(Collectors.toMap(CmsVideo::getVideoId, a -> a));
		// 遍历请求视频列表，修改的视频数据path变更需重新设置视频属性
		for (int i = 0; i < videoList.size(); i++) {
			CmsVideo video = videoList.get(i);
			if (IdUtils.validate(video.getVideoId())) {
				CmsVideo dbVideo = oldVideoMap.get(video.getVideoId());
				dbVideo.setTitle(video.getTitle());
				dbVideo.setRemark(video.getRemark());
				dbVideo.setSortFlag(i);
				if (!dbVideo.getPath().equals(video.getPath())) {
					dbVideo.setPath(video.getPath());
					dbVideo.setType(video.getType());
					if (!TYPE_SHARE.equals(video.getType())) {
						dbVideo.setType(FileExUtils.getExtension(dbVideo.getPath()).toUpperCase());
						this.getVideoService().progressVideoInfo(dbVideo);
					}
				}
				dbVideo.setCover(video.getCover());
				dbVideo.updateBy(this.getOperator().getUsername());
				this.getVideoService().updateById(dbVideo);
			} else {
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(this.getContentEntity().getContentId());
				video.setSiteId(this.getSiteId());
				video.setSortFlag(i);
				video.createBy(this.getOperator().getUsername());
				if (!TYPE_SHARE.equals(video.getType())) {
					video.setType(FileExUtils.getExtension(video.getPath()).toUpperCase());
					this.getVideoService().progressVideoInfo(video);
				}
				this.getVideoService().progressVideoInfo(video);
				this.getVideoService().save(video);
			}
		}
		return this.getContentEntity().getContentId();
	}

	@Override
	public void delete() {
		super.delete();
		if (this.hasExtendEntity()) {
			this.getVideoService().lambdaQuery().eq(CmsVideo::getContentId, this.getContentEntity().getContentId()).list()
					.forEach(video -> this.getVideoService().removeById(video));
		}
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);

		if (this.hasExtendEntity()) {
			Long newContentId = (Long) this.getParams().get("NewContentId");
			List<CmsVideo> videoList = this.getVideoService().getAlbumVideoList(this.getContentEntity().getContentId());
			for (CmsVideo video : videoList) {
				video.createBy(this.getOperator().getUsername());
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(newContentId);
				video.setSiteId(toCatalog.getSiteId());
				this.getVideoService().save(video);
			}
		}
	}

	private IVideoService getVideoService() {
		if (this.videoService == null) {
			this.videoService = SpringUtils.getBean(IVideoService.class);
		}
		return this.videoService;
	}
}
