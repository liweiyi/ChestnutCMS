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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.BASIC_MEMBER}")
public class MemberBasicVO {

    @XComment("{API.DOC.MEMBER.MEMBER_ID}")
    private Long memberId;

    @XComment("{API.DOC.MEMBER.USERNAME}")
    private String userName;

    @XComment("{API.DOC.MEMBER.NICK_NAME}")
    private String nickName;

    @XComment("{CC.ENTITY.STATUS}")
    private String status;

    public static MemberBasicVO from(Member member) {
        MemberBasicVO vo = new MemberBasicVO();
        vo.setMemberId(member.getMemberId());
        vo.setUserName(member.getUserName());
        vo.setNickName(member.getNickName());
        vo.setStatus(member.getStatus());
        return vo;
    }
}
