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
package com.chestnut.exmodel.service;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.exmodel.fixed.dict.ExtendModelDataType;
import com.chestnut.exmodel.properties.ContentExtendModelProperty;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 扩展模型服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class ExModelService {

    private final IModelService modelService;

    private final IModelDataService modelDataService;

    private final ICatalogService catalogService;

    private final Map<String, IMetaControlType> controlTypeMap;

    public List<XModelFieldDataDTO> getModelData(CmsContent content) {

        CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
        String modelId = ContentExtendModelProperty.getValue(catalog.getConfigProps());
        if (!NumberUtils.isCreatable(modelId)) {
            return List.of();
        }
        return this.getModelData(Long.valueOf(modelId), ExtendModelDataType.CONTENT, content.getContentId().toString());
    }

    public List<XModelFieldDataDTO> getModelData(Long modelId, String dataType, String dataId) {
        MetaModel metaModel = this.modelService.getMetaModel(modelId);
        if (Objects.isNull(metaModel)) {
            return List.of();
        }

        Map<String, Object> data = this.modelDataService.getModelDataByPkValue(modelId,
                Map.of(
                        CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
                        CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType,
                        CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
                ));

        List<XModelFieldDataDTO> list = new ArrayList<>();
        metaModel.getFields().forEach(f -> {
            Object fv = data.get(f.getCode());
            if(Objects.isNull(fv) || fv.toString().isEmpty()) {
                fv = f.getDefaultValue();
            }
            XModelFieldDataDTO dto = new XModelFieldDataDTO();
            dto.setLabel(f.getName());
            dto.setFieldName(CmsExtendMetaModelType.DATA_FIELD_PREFIX + f.getCode());
            dto.setControlType(f.getControlType());
            dto.setValue(Objects.requireNonNullElse(fv, StringUtils.EMPTY));
            dto.setOptions(XModelUtils.getOptions(f.getOptions()));
            dto.setValidations(f.getValidations());

            IMetaControlType controlType = controlTypeMap.get(IMetaControlType.BEAN_PREFIX + f.getControlType());
            controlType.parseFieldValue(dto);
            list.add(dto);
        });
        return list;
    }
}
