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
package com.chestnut.message.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import tools.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息推送配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CcMessageConfig.TABLE_NAME, autoResultMap = true)
public class CcMessageConfig extends BaseEntity {

    static final String TABLE_NAME = "cc_message_config";

    @TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 配置属性
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private ObjectNode configProps;
}
