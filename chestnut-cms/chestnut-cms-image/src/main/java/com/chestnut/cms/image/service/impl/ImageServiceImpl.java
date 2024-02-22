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
package com.chestnut.cms.image.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.mapper.CmsImageMapper;
import com.chestnut.cms.image.service.IImageService;

@Service
public class ImageServiceImpl extends ServiceImpl<CmsImageMapper, CmsImage> implements IImageService {

	@Override
	public List<CmsImage> getAlbumImages(Long contentId) {
		return this.lambdaQuery().eq(CmsImage::getContentId, contentId).list();
	}
}
