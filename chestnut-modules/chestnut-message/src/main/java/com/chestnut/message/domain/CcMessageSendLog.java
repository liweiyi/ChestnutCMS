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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 消息发送日志
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CcMessageSendLog.TABLE_NAME, autoResultMap = true)
public class CcMessageSendLog implements Serializable {

    static final String TABLE_NAME = "cc_message_send_log";

    @TableId(value = "log_id", type = IdType.INPUT)
    private String logId;

    /**
     * 发送配置ID
     */
    private Long configId;

    /**
     * 发送状态
     */
    private String status;

    /**
     * 结果信息
     */
    private String resultMessage;

    private String receiver;

    private Long sendTime;
}
