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
package com.chestnut.contentcore.domain.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TemplateListVO {

    private Long templateId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 发布通道编码
     */
    private String publishPipeCode;

    /**
     * 模板文件路径
     */
    private String path;

    /**
     * 模板文件大小
     */
    private Long filesize;
    
    private String filesizeName;
    
    /**
     * 模板文件最后变更时间
     */
    private LocalDateTime modifyTime;

    private String remark;
}
