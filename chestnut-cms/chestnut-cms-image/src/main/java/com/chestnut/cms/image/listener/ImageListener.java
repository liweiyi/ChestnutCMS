/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.cms.image.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.image.domain.BCmsImage;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.mapper.BCmsImageMapper;
import com.chestnut.cms.image.service.IImageService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageListener {

	private final IImageService imageService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除图集内容数据
		try {
			long total = this.imageService.dao().lambdaQuery()
					.eq(CmsImage::getSiteId, site.getSiteId()).count();
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除图片内容数据：" + (i * pageSize) + "/" + total);
				List<Long> imageIds = this.imageService.dao().lambdaQuery()
						.select(List.of(CmsImage::getImageId))
						.eq(CmsImage::getSiteId, site.getSiteId())
						.page(new Page<>(0, pageSize, false))
						.getRecords().stream().map(CmsImage::getImageId).toList();
				this.imageService.dao().removeBatchByIds(imageIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除图片内容错误：" + e.getMessage());
			log.error("Delete cms_image failed on site[{}] delete", site.getSiteId());
		}
		// 删除图集内容备份数据
		try {
			BCmsImageMapper backupMapper = this.imageService.dao().getBackupMapper();
			long total = backupMapper.selectCount(new LambdaQueryWrapper<BCmsImage>()
					.eq(BCmsImage::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除图片内容备份数据：" + (i * pageSize) + "/" + total);
				List<Long> backupIds = backupMapper.selectPage(
								new Page<>(0, pageSize, false),
							new LambdaQueryWrapper<BCmsImage>().select(List.of(BCmsImage::getImageId))
									.eq(BCmsImage::getSiteId, site.getSiteId())
						).getRecords().stream().map(BCmsImage::getBackupId).toList();
				backupMapper.deleteBatchIds(backupIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除图片内容备份错误：" + e.getMessage());
			log.error("Delete cms_image failed on site[{}] delete", site.getSiteId());
		}
	}
}
