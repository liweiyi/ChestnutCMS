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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.domain.vo.PageWidgetPrivVO;
import com.chestnut.contentcore.domain.vo.SitePrivVO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XComment("{API.DOC.CMS.PERMISSION.SAVE_PAGE_WIDGET_PERM_DTO}")
@Getter
@Setter
public class SavePageWidgetPermissionDTO {

    @XComment("{API.DOC.CMS.PERMISSION.OWNER_TYPE}")
    @NotEmpty
    private String ownerType;

    @XComment("{API.DOC.CMS.PERMISSION.OWNER}")
    @NotEmpty
    private String owner;

    @XComment("{API.DOC.CMS.PERMISSION.PERMS}")
    private List<PageWidgetPrivVO> perms;
}