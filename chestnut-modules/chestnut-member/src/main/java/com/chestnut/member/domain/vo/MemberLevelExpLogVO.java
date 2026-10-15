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
package com.chestnut.member.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.member.domain.MemberLevelExpLog;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.LEVEL_EXP_LOG}")
public class MemberLevelExpLogVO {

    @XComment("{API.DOC.MEMBER.LEVEL_LOG_ID}")
    private Long logId;

    @XComment("{API.DOC.MEMBER.OP_TYPE}")
    private String opType;

    @XComment("{API.DOC.MEMBER.LEVEL_OP_TYPE_NAME}")
    private String opTypeName;

    @XComment("{API.DOC.MEMBER.LEVEL_CHANGE_EXP}")
    private Integer changeExp;

    @XComment("{API.DOC.MEMBER.LEVEL}")
    private Integer level;

    @XComment("{API.DOC.MEMBER.LEVEL_CURRENT_EXP}")
    private Long exp;

    @XComment("{API.DOC.MEMBER.LEVEL_LOG_TIME}")
    private LocalDateTime logTime;

    public static MemberLevelExpLogVO from(MemberLevelExpLog log, String opTypeName) {
        MemberLevelExpLogVO vo = new MemberLevelExpLogVO();
        vo.setLogId(log.getLogId());
        vo.setOpType(log.getOpType());
        vo.setOpTypeName(opTypeName);
        vo.setChangeExp(log.getChangeExp());
        vo.setLevel(log.getLevel());
        vo.setExp(log.getExp());
        vo.setLogTime(log.getLogTime());
        return vo;
    }
}
