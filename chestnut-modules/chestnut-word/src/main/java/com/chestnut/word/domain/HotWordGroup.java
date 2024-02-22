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
package com.chestnut.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 热词分组表对应 [cms_hot_word_group]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(HotWordGroup.TABLE_NAME)
public class HotWordGroup extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cc_hot_word_group";

    @TableId(value = "group_id", type = IdType.INPUT)
    private Long groupId;

    /**
     * 所有者ID（扩展用）
     */
    private String owner;

    /**
     * 名称
     */
    @NotEmpty
    private String name;

    /**
     * 编码，唯一标识
     */
    @NotEmpty
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "热词分组编码只能使用大小写字母、数字和下划线")
    private String code;
    
    /**
     * 排序标识
     */
    private Long sortFlag;

    /**
     * 热词数量
     */
    private Long wordTotal;
}
