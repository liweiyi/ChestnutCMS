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
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.member.core.IMemberFavoriteDataType;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.MemberLike;
import com.chestnut.member.domain.MemberSignInLog;
import com.chestnut.member.domain.vo.MemberLikeLogVO;
import com.chestnut.member.domain.vo.MemberSignInLogVO;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.IMemberFavoritesService;
import com.chestnut.member.service.IMemberLikeService;
import com.chestnut.member.service.IMemberSignInLogService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@XComment("{API.DOC.MEMBER.INTERACTION_LOG_MODULE}")
@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberInteractionLog)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/interaction")
public class MemberInteractionLogController extends BaseRestController {

    private final IMemberLikeService memberLikeService;
    private final IMemberSignInLogService memberSignInLogService;
    private final IMemberFavoritesService memberFavoritesService;
    private final IMemberService memberService;

    @XComment("{API.DOC.MEMBER.INTERACTION_LIKE_LIST}")
    @GetMapping("/likes")
    public R<TableData<MemberLikeLogVO>> getLikeLogs(
            @RequestParam(required = false) @Positive @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @XComment("{API.DOC.MEMBER.INTERACTION_BEGIN_TIME}") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @XComment("{API.DOC.MEMBER.INTERACTION_END_TIME}") LocalDateTime endTime) {
        checkTimeRange(beginTime, endTime);
        PageRequest pr = this.getPageRequest();
        Page<MemberLike> page = this.memberLikeService.lambdaQuery()
                .eq(memberId != null, MemberLike::getMemberId, memberId)
                .ge(beginTime != null, MemberLike::getCreateTime, beginTime)
                .lt(endTime != null, MemberLike::getCreateTime, endTime == null ? null : endTime.plusSeconds(1))
                .orderByDesc(MemberLike::getCreateTime, MemberLike::getLogId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        Map<String, List<MemberLike>> groups = page.getRecords().stream().collect(Collectors.groupingBy(MemberLike::getDataType));
        Map<String, Map<String, String>> namesByType = new HashMap<>();
        groups.forEach((type, logs) -> {
            IMemberFavoriteDataType reader = this.memberFavoritesService.getMemberFavoriteDataType(type);
            if (reader == null) return;
            List<String> ids = logs.stream().map(log -> log.getDataId().toString()).distinct().toList();
            Map<String, String> names = new HashMap<>();
            reader.getDataList(ids).stream().filter(data -> Objects.equals(type, data.type()))
                    .forEach(data -> names.put(data.id(), data.name()));
            namesByType.put(type, names);
        });
        Map<Long, Member> members = getMembers(page.getRecords().stream().map(MemberLike::getMemberId).distinct().toList());
        List<MemberLikeLogVO> logs = page.getRecords().stream().map(log -> MemberLikeLogVO.from(log,
                namesByType.getOrDefault(log.getDataType(), Map.of()).get(log.getDataId().toString()), members.get(log.getMemberId()))).toList();
        return this.bindDataTable(logs, page.getTotal());
    }

    @XComment("{API.DOC.MEMBER.INTERACTION_SIGN_IN_LIST}")
    @GetMapping("/signIns")
    public R<TableData<MemberSignInLogVO>> getSignInLogs(
            @RequestParam(required = false) @Positive @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @XComment("{API.DOC.MEMBER.INTERACTION_BEGIN_TIME}") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @XComment("{API.DOC.MEMBER.INTERACTION_END_TIME}") LocalDateTime endTime) {
        checkTimeRange(beginTime, endTime);
        PageRequest pr = this.getPageRequest();
        Page<MemberSignInLog> page = this.memberSignInLogService.lambdaQuery()
                .eq(memberId != null, MemberSignInLog::getMemberId, memberId)
                .ge(beginTime != null, MemberSignInLog::getLogTime, beginTime)
                .lt(endTime != null, MemberSignInLog::getLogTime, endTime == null ? null : endTime.plusSeconds(1))
                .orderByDesc(MemberSignInLog::getLogTime, MemberSignInLog::getLogId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        Map<Long, Member> members = getMembers(page.getRecords().stream().map(MemberSignInLog::getMemberId).distinct().toList());
        return this.bindDataTable(page.getRecords().stream()
                .map(log -> MemberSignInLogVO.from(log, members.get(log.getMemberId()))).toList(), page.getTotal());
    }

    private Map<Long, Member> getMembers(List<Long> memberIds) {
        if (memberIds.isEmpty()) return Map.of();
        return this.memberService.lambdaQuery().select(Member::getMemberId, Member::getUserName, Member::getNickName)
                .in(Member::getMemberId, memberIds).list().stream().collect(Collectors.toMap(Member::getMemberId, member -> member));
    }

    private void checkTimeRange(LocalDateTime beginTime, LocalDateTime endTime) {
        Assert.isTrue(beginTime == null || endTime == null || !beginTime.isAfter(endTime),
                () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("endTime"));
    }
}
