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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容操作记录 [cms_content_op_log]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsContentOpLog.TABLE_NAME)
public class CmsContentOpLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_content_op_log";

    @TableId(value = "log_id", type = IdType.INPUT)
    @XComment("ID")
    private Long logId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("所属栏目ID")
    private Long catalogId;

    @XComment("所属栏目祖级IDs")
    private String catalogAncestors;

    @XComment("所属内容ID")
    private Long contentId;

    @XComment("操作类型")
    private String type;

    @XComment("操作明细")
    private String details;

    @XComment("操作人类型")
    private String operatorType;

    @XComment("操作人用户名")
    private String operator;

    @XComment("日志时间")
    private LocalDateTime logTime;
}
