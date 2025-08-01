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
package com.chestnut.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 标签分组表 [cc_tag_word_group]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(TagWordGroup.TABLE_NAME)
public class TagWordGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cc_tag_word_group";

    @XComment("{CC.TAG_WORD_GROUP.ID}")
    @TableId(value = "group_id", type = IdType.INPUT)
    private Long groupId;

    @XComment("{CC.TAG_WORD_GROUP.OWNER}")
    private String owner;

    @XComment("{CC.TAG_WORD_GROUP.PARENT_ID}")
    private Long parentId;

    @XComment("{CC.TAG_WORD_GROUP.NAME}")
    private String name;

    @XComment("{CC.TAG_WORD_GROUP.CODE}")
    private String code;

    @XComment("{CC.ENTITY.SORT}")
    private Long sortFlag;

    @XComment("{CC.TAG_WORD_GROUP.TOTAL}")
    private Long wordTotal;
}
