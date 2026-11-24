package com.chestnut.member.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.MemberSubscribeConsts;
import com.chestnut.member.domain.MemberSubscribeLog;
import com.chestnut.member.domain.MemberSubscribeRecord;
import com.chestnut.member.domain.dto.AdjustSubscribeDurationReq;
import com.chestnut.member.domain.dto.QueryMemberSubscribeReq;
import com.chestnut.member.domain.dto.QuerySubscribeDurationLogReq;
import com.chestnut.member.mapper.MemberSubscribeLogMapper;
import com.chestnut.member.permission.MemberSubscribePriv;
import com.chestnut.member.service.IMemberSubscribeRecordService;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * 会员订阅关联、结束时间调整及审计查询入口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.RECORD_MODULE}")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/member/subscription")
public class MemberSubscribeController extends BaseRestController {

    private final IMemberSubscribeRecordService subscriptionService;

    private final MemberSubscribeLogMapper subscriptionLogMapper;

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.LIST}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.RECORD_VIEW)
    @GetMapping("/record/list")
    public R<TableData<MemberSubscribeRecord>> list(
            @ModelAttribute @Validated QueryMemberSubscribeReq req) {
        PageRequest pr = getPageRequest();
        LambdaQueryChainWrapper<MemberSubscribeRecord> q =
                this.subscriptionService
                        .lambdaQuery()
                        .eq(
                                IdUtils.validate(req.getMemberId()),
                                MemberSubscribeRecord::getMemberId,
                                req.getMemberId())
                        .eq(
                                StringUtils.isNotBlank(req.getSubscriptionId()),
                                MemberSubscribeRecord::getSubscriptionId,
                                req.getSubscriptionId());
        long now = Instant.now().getEpochSecond();
        if (StringUtils.isNotEmpty(req.getValid())) {
            if (YesOrNo.isYes(req.getValid())) {
                q.gt(MemberSubscribeRecord::getExpireTime, now);
            } else {
                q.le(MemberSubscribeRecord::getExpireTime, now);
            }
        }
        Page<MemberSubscribeRecord> page = q.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
        return bindDataTable(page);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.ADJUST}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.ADJUST)
    @Log(title = "会员订阅时长调整", businessType = BusinessType.UPDATE)
    @PostMapping("/record/adjust")
    public R<MemberSubscribeLog> adjust(@RequestBody @Validated AdjustSubscribeDurationReq req) {
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        req.setSourceType(MemberSubscribeConsts.SUBSCRIBE_SOURCE_TYPE_MANUAL);
        // 手动调整的业务来源按请求ID唯一，操作人另由可信上下文记录。
        req.setSourceId(req.getRequestId());
        MemberSubscribeLog log = subscriptionService.adjustDuration(req, Operator.of(loginUser));
        return R.ok(log);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.LOGS}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.LOG_VIEW)
    @GetMapping("/log/list")
    public R<TableData<MemberSubscribeLog>> logs(
            @ModelAttribute @Validated QuerySubscribeDurationLogReq req) {
        PageRequest pr = getPageRequest();
        Page<MemberSubscribeLog> page =
                new LambdaQueryChainWrapper<>(subscriptionLogMapper)
                        .eq(
                                IdUtils.validate(req.getMemberId()),
                                MemberSubscribeLog::getMemberId,
                                req.getMemberId())
                        .eq(
                                StringUtils.isNotBlank(req.getSubscriptionId()),
                                MemberSubscribeLog::getSubscriptionId,
                                req.getSubscriptionId())
                        .orderByDesc(MemberSubscribeLog::getOperationTime, MemberSubscribeLog::getLogId)
                        .page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
        return this.bindDataTable(page);
    }
}
