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
package com.chestnut.cms.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.service.IImageService;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;

import java.util.List;

public class ImageContent extends AbstractContent<List<CmsImage>> {

	private IImageService imageService;

	@Override
	public String getContentType() {
		return ImageContentType.ID;
	}

	@Override
	protected void add0() {
		if (!hasExtendEntity()) {
			return;
		}
		List<CmsImage> images = this.getExtendEntity();
		if (StringUtils.isNotEmpty(images)) {
			for (int i = 0; i < images.size(); i++) {
				CmsImage image = images.get(i);
				image.setImageId(IdUtils.getSnowflakeId());
				image.setContentId(this.getContentEntity().getContentId());
				image.setSiteId(this.getContentEntity().getSiteId());
				image.setImageType(FileExUtils.getExtension(image.getPath()));
				image.setSortFlag(i);
				image.createBy(this.getOperatorUName());
			}
			this.getImageService().dao().saveBatch(images);
		}
	}

	@Override
	protected void save0() {
		if (!this.hasExtendEntity()) {
			this.getImageService().dao().remove(new LambdaQueryWrapper<CmsImage>().eq(CmsImage::getContentId,
					this.getContentEntity().getContentId()));
			return;
		}
		// 图片数处理
		List<CmsImage> imageList = this.getExtendEntity();
		// 先删除数据
		List<Long> updateImageIds = imageList.stream().map(CmsImage::getImageId)
				.filter(IdUtils::validate).toList();
		this.getImageService().dao()
				.remove(new LambdaQueryWrapper<CmsImage>()
						.eq(CmsImage::getContentId, this.getContentEntity().getContentId())
						.notIn(!updateImageIds.isEmpty(), CmsImage::getImageId, updateImageIds));
		for (int i = 0; i < imageList.size(); i++) {
			CmsImage image = imageList.get(i);
			if (IdUtils.validate(image.getImageId())) {
				image.setSortFlag(i);
				image.updateBy(this.getOperatorUName());
				this.getImageService().dao().updateById(image);
			} else {
				image.setImageId(IdUtils.getSnowflakeId());
				image.setContentId(this.getContentEntity().getContentId());
				image.setSiteId(this.getContentEntity().getSiteId());
				image.setImageType(FileExUtils.getExtension(image.getPath()));
				image.setSortFlag(i);
				image.createBy(this.getOperatorUName());
				this.getImageService().dao().save(image);
			}
		}
	}

	@Override
	protected void delete0() {
		if (this.hasExtendEntity()) {
			this.getImageService().dao().deleteByContentIdAndBackup(
					this.getContentEntity().getContentId(),
					this.getOperatorUName()
				);
		}
	}

	@Override
	public void copyTo0(CmsContent newContent, Integer copyType) {
		if (this.hasExtendEntity() && ContentCopyType.isIndependency(copyType)) {
			List<CmsImage> albumImages = this.getImageService().getAlbumImages(this.getContentEntity().getContentId());
			for (CmsImage image : albumImages) {
				image.createBy(this.getOperatorUName());
				image.setImageId(IdUtils.getSnowflakeId());
				image.setContentId(newContent.getContentId());
				this.getImageService().dao().save(image);
			}
		}
    }

	private IImageService getImageService() {
		if (this.imageService == null) {
			this.imageService = SpringUtils.getBean(IImageService.class);
		}
		return this.imageService;
	}
}
