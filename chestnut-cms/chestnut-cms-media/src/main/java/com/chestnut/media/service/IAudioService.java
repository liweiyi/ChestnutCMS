package com.chestnut.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.media.domain.CmsAudio;

import java.util.List;

public interface IAudioService extends IService<CmsAudio> {

	/**
	 * 获取音频集音频列表数据
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsAudio> getAlbumAudioList(Long contentId);

	/**
	 * 处理音频信息
	 * 
	 * @param audio
	 */
	void progressAudioInfo(CmsAudio audio);
}
