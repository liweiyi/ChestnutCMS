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
package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.JacksonUtils;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serial;

/**
 * 会员配置。
 */
@XComment("{API.DOC.MEMBER.CONFIG_ENTITY}")
@Getter
@Setter
@TableName(value = MemberConfig.TABLE_NAME, autoResultMap = true)
public class MemberConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "cc_member_config";

    @XComment("{API.DOC.MEMBER.CONFIG_ID}")
    @TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;

    @XComment("{API.DOC.MEMBER.CONFIGS}")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private ObjectNode configs;

    public ObjectNode getConfigs() {
        if (this.configs == null) {
            this.configs = JacksonUtils.objectNode();
        }
        return this.configs;
    }
}
