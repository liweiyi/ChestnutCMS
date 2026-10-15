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
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.MemberLike;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.INTERACTION_LIKE_RECORD}")
public class MemberLikeLogVO {

    @XComment("{API.DOC.MEMBER.INTERACTION_LOG_ID}")
    private Long logId;

    @XComment("{API.DOC.MEMBER.MEMBER_ID}")
    private Long memberId;

    @XComment("{API.DOC.MEMBER.USERNAME}")
    private String userName;

    @XComment("{API.DOC.MEMBER.NICK_NAME}")
    private String nickName;

    @XComment("{API.DOC.MEMBER.DATA_TYPE}")
    private String dataType;

    @XComment("{API.DOC.MEMBER.DATA_ID}")
    private Long dataId;

    @XComment("{API.DOC.MEMBER.INTERACTION_DATA_NAME}")
    private String name;

    @XComment("{API.DOC.MEMBER.INTERACTION_LIKE_TIME}")
    private LocalDateTime createTime;

    public static MemberLikeLogVO from(MemberLike log, String name, Member member) {
        MemberLikeLogVO vo = new MemberLikeLogVO();
        vo.setLogId(log.getLogId());
        vo.setMemberId(log.getMemberId());
        if (member != null) {
            vo.setUserName(member.getUserName());
            vo.setNickName(member.getNickName());
        }
        vo.setDataType(log.getDataType());
        vo.setDataId(log.getDataId());
        vo.setName(name);
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
