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
package com.chestnut.cms.member.domain.vo;

import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
public class ContentDynamicDataWithContributorVO extends ContentDynamicDataVO {

    private ContributorVO contributor;

    public ContentDynamicDataWithContributorVO(ContentDynamicDataVO vo) {
        this.setContentId(vo.getContentId());
        this.setContributorId(vo.getContributorId());
        this.setLikes(vo.getLikes());
        this.setComments(vo.getComments());
        this.setViews(vo.getViews());
        this.setFavorites(vo.getFavorites());
    }
}
