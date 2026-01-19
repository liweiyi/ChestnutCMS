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
package com.chestnut.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 标签对象 [cms_tag_word]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(TagWord.TABLE_NAME)
public class TagWord extends BaseEntity {

    @Serial
    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cc_tag_word";

    @XComment("{CC.TAG_WORD.ID}")
    @TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;

    @XComment("{CC.TAG_WORD.OWNER}")
    private String owner;

    @XComment("{CC.TAG_WORD.GROUP_ID}")
    private Long groupId;

    @XComment("{CC.TAG_WORD.WORD}")
    private String word;

    @XComment("{CC.TAG_WORD.LOGO}")
    private String logo;

    @XComment("{CC.TAG_WORD.USE_COUNT}")
    private Long useCount;

    @XComment("{CC.TAG_WORD.HIT_COUNT}")
    private Long hitCount;

    /**
     * 图片预览路径
     */
    @TableField(exist = false)
    private String src;
    
    @XComment("{CC.ENTITY.SORT}")
    private Long sortFlag;
}
