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
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.vo.MemberBasicVO;
import com.chestnut.member.domain.vo.MemberFavoriteVO;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.IMemberFavoritesService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@XComment("{API.DOC.MEMBER.FAVORITES_MANAGEMENT}")
@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberFavorites)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/favorites")
public class MemberFavoritesController extends BaseRestController {

    private final IMemberService memberService;
    private final IMemberFavoritesService memberFavoritesService;

    @XComment("{API.DOC.MEMBER.FAVORITES_MEMBERS}")
    @GetMapping("/members")
    public R<TableData<MemberBasicVO>> getMembers(
            @RequestParam(required = false) @Positive @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam(required = false) @Length(max = 30) @XComment("{API.DOC.MEMBER.LEVEL_MEMBER_KEYWORD}") String keyword) {
        PageRequest pr = this.getPageRequest();
        Page<Member> page = this.memberService.lambdaQuery()
                .select(Member::getMemberId, Member::getUserName, Member::getNickName, Member::getStatus)
                .eq(memberId != null, Member::getMemberId, memberId)
                .and(StringUtils.isNotBlank(keyword), q -> q.like(Member::getUserName, keyword).or().like(Member::getNickName, keyword))
                .orderByDesc(Member::getMemberId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page.getRecords().stream().map(MemberBasicVO::from).toList(), page.getTotal());
    }

    @XComment("{API.DOC.MEMBER.FAVORITES_LIST}")
    @GetMapping("/list")
    public R<TableData<MemberFavoriteVO>> getFavorites(@RequestParam @LongId @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId) {
        PageRequest pr = this.getPageRequest();
        return this.bindDataTable(this.memberFavoritesService.getMemberFavoritesPage(memberId, pr.getPageNumber(), pr.getPageSize()));
    }
}
