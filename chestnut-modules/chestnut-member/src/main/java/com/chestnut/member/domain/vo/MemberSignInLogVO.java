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
import com.chestnut.member.domain.MemberSignInLog;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.INTERACTION_SIGN_IN_RECORD}")
public class MemberSignInLogVO {

    @XComment("{API.DOC.MEMBER.INTERACTION_LOG_ID}")
    private Long logId;

    @XComment("{API.DOC.MEMBER.MEMBER_ID}")
    private Long memberId;

    @XComment("{API.DOC.MEMBER.USERNAME}")
    private String userName;

    @XComment("{API.DOC.MEMBER.NICK_NAME}")
    private String nickName;

    @XComment("{API.DOC.MEMBER.INTERACTION_SIGN_IN_DATE}")
    private Integer signInKey;

    @XComment("{API.DOC.MEMBER.INTERACTION_RECORD_TIME}")
    private LocalDateTime logTime;

    public static MemberSignInLogVO from(MemberSignInLog log, Member member) {
        MemberSignInLogVO vo = new MemberSignInLogVO();
        vo.setLogId(log.getLogId());
        vo.setMemberId(log.getMemberId());
        if (member != null) {
            vo.setUserName(member.getUserName());
            vo.setNickName(member.getNickName());
        }
        vo.setSignInKey(log.getSignInKey());
        vo.setLogTime(log.getLogTime());
        return vo;
    }
}
