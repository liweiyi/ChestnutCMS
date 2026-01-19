/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.media.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.media.domain.dto.VideoScreenshotDTO;
import com.chestnut.media.service.IVideoService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.schild.jave.EncoderException;

import java.io.IOException;

/**
 * <p>
 * 视频内容前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/video")
@RequiredArgsConstructor
public class VideoController extends CmsRestController {

	private final IVideoService videoService;

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/screenshot")
	public R<?> screenshot(@RequestBody  @Validated VideoScreenshotDTO dto)
			throws EncoderException, IOException {
		CmsSite site = this.getCurrentSite();
		CmsResource cmsResource = this.videoService.videoScreenshot(site, dto.getPath(),
				dto.getTimestamp(), StpAdminUtil.getLoginUser());
		return R.ok(cmsResource);
	}
}
