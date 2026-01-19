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
package com.chestnut.customform.service.impl;

import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.exception.CustomFormErrorCode;
import com.chestnut.customform.rule.ICustomFormLimitRule;
import com.chestnut.customform.service.ICustomFormApiService;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.system.SysConstants;
import com.chestnut.xmodel.service.IModelDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * CustomFormApiServiceImpl
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class CustomFormApiServiceImpl implements ICustomFormApiService {

    private final ICustomFormService customFormService;

    private final IModelDataService modelDataService;

    private final IResourceService resourceService;

    private final ISiteService siteService;

    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void submit(CmsCustomForm form, Map<String, Object> formData) throws IOException {
        String uuid = MapUtils.getString(formData, CmsCustomFormMetaModelType.FIELD_UUID.getCode());
        RLock lock = redissonClient.getLock("CustomFormSubmit-" + uuid);
        lock.lock();
        try {
            // 唯一提交限制校验
            ICustomFormLimitRule limitRule = customFormService.getLimitRule(form.getRuleLimit());
            if (Objects.nonNull(limitRule)) {
                if (!limitRule.check(form, formData)) {
                    throw CustomFormErrorCode.CANNOT_RESUBMIT.exception();
                }
            }
            // base64图片保存到资源库
            CmsSite site = siteService.getSite(form.getSiteId());
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                if (ImageUtils.isBase64Image(entry.getValue())) {
                    CmsResource resource = resourceService.addBase64Image(site, SysConstants.SYS_OPERATOR, entry.getValue().toString());
                    entry.setValue(InternalDataType_Resource.getInternalUrl(resource));
                }
            }
            // 保存数据
            this.modelDataService.saveModelData(form.getModelId(), formData);
        } finally {
            lock.unlock();
        }
    }
}
