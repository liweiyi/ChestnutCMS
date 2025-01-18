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
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.contentcore.domain.dto.ImageCropDTO;
import com.chestnut.contentcore.domain.dto.ImageRotateDTO;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IImageProcessService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 图片资源处理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.ResourceView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
)
@RestController
@RequestMapping("/cms/process/image")
@RequiredArgsConstructor
public class ImageProcessController extends BaseRestController {

	private final IImageProcessService imageProcessService;

	@Log(title = "图片裁剪", businessType = BusinessType.UPDATE)
	@PostMapping("/crop")
	public R<?> cropImage(@RequestBody @Validated ImageCropDTO dto) throws IOException {
		this.imageProcessService.cropImage(dto);
		return R.ok();
	}

	@Log(title = "旋转缩放", businessType = BusinessType.UPDATE)
	@PostMapping("/rotate")
	public R<?> rotateImage(@RequestBody @Validated ImageRotateDTO dto) throws IOException {
		this.imageProcessService.rotateImage(dto);
		return R.ok();
	}

	@Log(title = "文字水印", businessType = BusinessType.UPDATE)
	@PostMapping("/image/textWatermark")
	public R<?> textWatermark(@RequestBody @Validated ImageCropDTO dto) throws IOException {

		return R.ok();
	}

	@Log(title = "图片水印", businessType = BusinessType.UPDATE)
	@PostMapping("/image/imageWatermark")
	public R<?> imageWatermark(@RequestBody @Validated ImageCropDTO dto) throws IOException {

		return R.ok();
	}
}
