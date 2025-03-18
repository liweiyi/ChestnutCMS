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
package com.chestnut.contentcore.domain;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;

public interface InitByContent {

    void setAttributes(String[] attributes);

    void setLogo(String logo);

    void setLogoSrc(String logoSrc);

    void setImages(List<String> images);

    void setImagesSrc(List<String> imagesSrc);

    default void initByContent(CmsContent content, boolean preview) {
        BeanUtils.copyProperties(content, this);
        this.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
        if (StringUtils.isNotEmpty(content.getImages())) {
            this.setLogo(content.getImages().get(0));
            if (preview) {
                this.setImagesSrc(content.getImages().stream().map(InternalUrlUtils::getActualPreviewUrl).toList());
                this.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(content.getLogo()));
            }
        } else {
            this.setImages(List.of());
            this.setImagesSrc(List.of());
        }
    }
}
