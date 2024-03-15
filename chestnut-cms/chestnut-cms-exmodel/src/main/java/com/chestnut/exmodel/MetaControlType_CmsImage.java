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
package com.chestnut.exmodel;

import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 元数据模型字段类型：图片上传
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_CmsImage.TYPE)
public class MetaControlType_CmsImage implements IMetaControlType {

    public static final String TYPE = "CMSImage";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }

    @Override
    public void parseFieldValue(XModelFieldDataDTO fieldData) {
        Object value = fieldData.getValue();
        if (Objects.isNull(value)) {
            return;
        }
        String imagePath = value.toString();
        if (InternalUrlUtils.isInternalUrl(imagePath)) {
            String previewUrl = InternalUrlUtils.getActualPreviewUrl(imagePath);
            fieldData.setValueObj(previewUrl);
        } else {
            fieldData.setValueObj(imagePath);
        }
    }
}
