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
package com.chestnut.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.dto.CancelMemberFollowRequest;
import com.chestnut.member.domain.vo.MemberFollowVO;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.IMemberFollowService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@XComment("{API.DOC.MEMBER.FOLLOW_MANAGEMENT}")
@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberFollow)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/follow")
public class MemberFollowController extends BaseRestController {

    private final IMemberService memberService;

    private final IMemberFollowService memberFollowService;

    @XComment("{API.DOC.MEMBER.FOLLOW_LIST}")
    @GetMapping("/list")
    public R<TableData<MemberFollowVO>> getPageList(
            @RequestParam @LongId @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam(defaultValue = "follow") @Pattern(regexp = "follow|follower")
            @XComment("{API.DOC.MEMBER.FOLLOW_TYPE}") String type,
            @RequestParam(required = false) @Positive @XComment("{API.DOC.MEMBER.TARGET_ID}") Long targetMemberId,
            @RequestParam(required = false) @Length(max = 30) @XComment("{API.DOC.MEMBER.NICK_NAME}") String nickName) {
        PageRequest pr = this.getPageRequest();
        // EXISTS keeps filtering and pagination in the database without loading all related IDs.
        String relation = "follower".equals(type)
                ? "SELECT 1 FROM cc_member_follow f WHERE f.follow_member_id = {0} AND f.member_id = cc_member.member_id"
                : "SELECT 1 FROM cc_member_follow f WHERE f.member_id = {0} AND f.follow_member_id = cc_member.member_id";
        Page<Member> page = this.memberService.lambdaQuery()
                .select(Member::getMemberId, Member::getUserName, Member::getNickName)
                .exists(relation, memberId)
                .eq(targetMemberId != null, Member::getMemberId, targetMemberId)
                .like(StringUtils.isNotBlank(nickName), Member::getNickName, nickName)
                .orderByDesc(Member::getMemberId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page.getRecords().stream().map(MemberFollowVO::from).toList(), page.getTotal());
    }

    @XComment("{API.DOC.MEMBER.FOLLOW_CANCEL}")
    @Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberFollowCancel)
    @Log(title = "取消会员关注关系", businessType = BusinessType.DELETE)
    @PostMapping("/cancel")
    public R<Void> cancel(@RequestBody @Validated CancelMemberFollowRequest req) {
        if ("follower".equals(req.getType())) {
            this.memberFollowService.cancelFollow(req.getTargetMemberId(), req.getMemberId());
        } else {
            this.memberFollowService.cancelFollow(req.getMemberId(), req.getTargetMemberId());
        }
        return R.ok();
    }
}
