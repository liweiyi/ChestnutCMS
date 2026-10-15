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
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.domain.MemberLevelConfig;
import com.chestnut.member.domain.MemberLevelExpLog;
import com.chestnut.member.domain.vo.ExpOperationVO;
import com.chestnut.member.domain.vo.MemberLevelDetailVO;
import com.chestnut.member.domain.vo.MemberLevelExpLogVO;
import com.chestnut.member.domain.vo.MemberBasicVO;
import com.chestnut.member.level.IExpOperation;
import com.chestnut.member.level.ILevelType;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.*;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@XComment("{API.DOC.MEMBER.LEVEL_MANAGEMENT}")
@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberLevelManage)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/level")
public class MemberLevelController extends BaseRestController {

    private final IMemberService memberService;
    private final IMemberLevelService memberLevelService;
    private final IMemberLevelConfigService memberLevelConfigService;
    private final IMemberLevelExpLogService memberLevelExpLogService;
    private final IMemberExpConfigService memberExpConfigService;

    @XComment("{API.DOC.MEMBER.LEVEL_MEMBERS}")
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

    @XComment("{API.DOC.MEMBER.LEVEL_DETAILS}")
    @GetMapping("/list")
    public R<List<MemberLevelDetailVO>> getLevels(@RequestParam @LongId @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId) {
        Assert.isTrue(this.memberService.lambdaQuery().eq(Member::getMemberId, memberId).count() > 0,
                () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("memberId", memberId));
        // Read directly: looking at an untouched level must not create a member-level record.
        Map<String, MemberLevel> levels = this.memberLevelService.lambdaQuery().eq(MemberLevel::getMemberId, memberId)
                .list().stream().collect(Collectors.toMap(MemberLevel::getLevelType, level -> level));
        Map<String, ILevelType> types = this.memberLevelConfigService.getLevelTypes().values().stream()
                .collect(Collectors.toMap(ILevelType::getId, type -> type));
        Map<String, List<MemberLevelConfig>> configs = this.memberLevelConfigService.lambdaQuery()
                .select(MemberLevelConfig::getLevelType, MemberLevelConfig::getLevel, MemberLevelConfig::getName, MemberLevelConfig::getNextNeedExp)
                .list().stream().collect(Collectors.groupingBy(MemberLevelConfig::getLevelType));
        TreeSet<String> typeIds = new TreeSet<>(types.keySet());
        typeIds.addAll(levels.keySet());
        return R.ok(typeIds.stream().map(type -> MemberLevelDetailVO.from(type,
                types.containsKey(type) ? I18nUtils.get(types.get(type).getName()) : type,
                levels.get(type), configs.getOrDefault(type, List.of()))).toList());
    }

    @XComment("{API.DOC.MEMBER.LEVEL_LOGS}")
    @GetMapping("/logs")
    public R<TableData<MemberLevelExpLogVO>> getLogs(
            @RequestParam @LongId @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam @NotBlank @Length(max = 30) @XComment("{API.DOC.MEMBER.LEVEL_TYPE}") String levelType,
            @RequestParam(required = false) @Length(max = 50) @XComment("{API.DOC.MEMBER.OP_TYPE}") String opType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @XComment("{API.DOC.MEMBER.LEVEL_BEGIN_DATE}") LocalDate beginDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @XComment("{API.DOC.MEMBER.LEVEL_END_DATE}") LocalDate endDate) {
        Assert.isTrue(beginDate == null || endDate == null || !beginDate.isAfter(endDate),
                () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("endDate"));
        PageRequest pr = this.getPageRequest();
        Page<MemberLevelExpLog> page = this.memberLevelExpLogService.lambdaQuery()
                .eq(MemberLevelExpLog::getMemberId, memberId)
                .eq(MemberLevelExpLog::getLevelType, levelType)
                .eq(StringUtils.isNotBlank(opType), MemberLevelExpLog::getOpType, opType)
                .ge(beginDate != null, MemberLevelExpLog::getLogTime, beginDate == null ? null : beginDate.atStartOfDay())
                .lt(endDate != null, MemberLevelExpLog::getLogTime, endDate == null ? null : endDate.plusDays(1).atStartOfDay())
                .orderByDesc(MemberLevelExpLog::getLogTime, MemberLevelExpLog::getLogId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        Map<String, IExpOperation> operations = this.memberExpConfigService.getExpOperations();
        return this.bindDataTable(page.getRecords().stream().map(log -> {
            IExpOperation operation = operations.get(IExpOperation.BEAN_PREFIX + log.getOpType());
            return MemberLevelExpLogVO.from(log, operation == null ? log.getOpType() : I18nUtils.get(operation.getName()));
        }).toList(), page.getTotal());
    }

    @XComment("{API.DOC.MEMBER.GET_EXP_OP_TYPES}")
    @GetMapping("/operations")
    public R<List<ExpOperationVO>> getOperations(
            @RequestParam @LongId @XComment("{API.DOC.MEMBER.MEMBER_ID}") Long memberId,
            @RequestParam @NotBlank @Length(max = 30) @XComment("{API.DOC.MEMBER.LEVEL_TYPE}") String levelType) {
        Map<String, String> names = new TreeMap<>();
        this.memberExpConfigService.getExpOperations().values().forEach(op -> names.put(op.getId(), I18nUtils.get(op.getName())));
        // Retain filters for historical operations even after their implementation is removed.
        this.memberLevelExpLogService.lambdaQuery().select(MemberLevelExpLog::getOpType)
                .eq(MemberLevelExpLog::getMemberId, memberId).eq(MemberLevelExpLog::getLevelType, levelType)
                .groupBy(MemberLevelExpLog::getOpType).list().forEach(log -> names.putIfAbsent(log.getOpType(), log.getOpType()));
        return R.ok(names.entrySet().stream().map(e -> new ExpOperationVO(e.getKey(), e.getValue())).toList());
    }
}
