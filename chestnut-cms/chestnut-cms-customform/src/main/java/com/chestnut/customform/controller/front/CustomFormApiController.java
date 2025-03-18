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
package com.chestnut.customform.controller.front;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.SysConstants;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.xmodel.service.IModelDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 自定义表单API控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/api/customform")
@RequiredArgsConstructor
public class CustomFormApiController extends BaseRestController {

	private final ICustomFormService customFormService;

	private final IModelDataService modelDataService;

	private final IResourceService resourceService;

	private final ISiteService siteService;

	@IgnoreDemoMode
	@PostMapping("/submit")
	public R<?> submitForm(@RequestBody @Validated Map<String, Object> formData) {
		Long formId = MapUtils.getLong(formData, "formId");
		if (!IdUtils.validate(formId)) {
			return R.fail("Unknown form: " + formId);
		}
		CmsCustomForm form = this.customFormService.getById(formId);
		if (Objects.isNull(form)) {
			return R.fail("Unknown form: " + formId);
		}
		if (YesOrNo.isYes(form.getNeedLogin()) && !StpMemberUtil.isLogin()) {
			return R.fail("Please login first.");
		}
		// TODO 限制规则校验：验证码，IP，浏览器指纹
		if (YesOrNo.isYes(form.getNeedCaptcha())) {

		}
		String uuid = MapUtils.getString(formData, "uuid", StringUtils.EMPTY);
		String clientIp = ServletUtils.getIpAddr(ServletUtils.getRequest());

		formData.put(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), IdUtils.getSnowflakeId());
		formData.put(CmsCustomFormMetaModelType.FIELD_MODEL_ID.getCode(), form.getFormId());
		formData.put(CmsCustomFormMetaModelType.FIELD_SITE_ID.getCode(), form.getSiteId());
		formData.put(CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getCode(), clientIp);
		formData.put(CmsCustomFormMetaModelType.FIELD_UUID.getCode(), uuid);
		formData.put(CmsCustomFormMetaModelType.FIELD_CREATE_TIME.getCode(), LocalDateTime.now());

		CmsSite site = siteService.getSite(form.getSiteId());

		formData.forEach((k, v) -> {
			if (Objects.nonNull(v) && v.toString().startsWith("data:image/png;base64,")) {
                try {
					CmsResource resource = resourceService.addBase64Image(site, SysConstants.SYS_OPERATOR, v.toString());
					formData.put(k, InternalDataType_Resource.getInternalUrl(resource));
				} catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
		});

		this.modelDataService.saveModelData(form.getModelId(), formData);
		return R.ok();
	}
}
