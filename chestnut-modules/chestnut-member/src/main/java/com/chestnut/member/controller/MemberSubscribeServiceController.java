package com.chestnut.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.member.domain.MemberSubscribeService;
import com.chestnut.member.domain.dto.QueryMemberSubscribeServiceReq;
import com.chestnut.member.domain.dto.SaveMemberSubscribeServiceReq;
import com.chestnut.member.permission.MemberSubscribePriv;
import com.chestnut.member.service.IMemberSubscribeServiceService;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员订阅服务项管理及订阅定义服务范围选项。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.MODULE}")
@RequiredArgsConstructor
@RestController
@Validated
@Priv(type = AdminUserType.TYPE)
@RequestMapping("/member-subscribe-service")
public class MemberSubscribeServiceController extends BaseRestController {
    private final IMemberSubscribeServiceService service;

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.LIST}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.CONFIG_VIEW)
    @GetMapping("/list")
    public R<TableData<MemberSubscribeService>> list(@Valid @ModelAttribute QueryMemberSubscribeServiceReq req) {
        var pr = getPageRequest();
        Page<MemberSubscribeService> page = service.lambdaQuery()
                .and(StringUtils.isNotBlank(req.getKeyword()), and ->
                        and.like(MemberSubscribeService::getServiceId, req.getKeyword())
                                .or().like(MemberSubscribeService::getName, req.getKeyword()))
                .orderByAsc(MemberSubscribeService::getServiceId)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
        return bindDataTable(page);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.OPTIONS}")
    @GetMapping("/options")
    public R<List<SelectOption>> options(@Valid @ModelAttribute QueryMemberSubscribeServiceReq req) {
        Page<MemberSubscribeService> page = service.lambdaQuery()
                .select(MemberSubscribeService::getServiceId, MemberSubscribeService::getName)
                .in(req.getIds() != null && !req.getIds().isEmpty(), MemberSubscribeService::getServiceId, req.getIds())
                .and(StringUtils.isNotBlank(req.getKeyword()), and ->
                        and.like(MemberSubscribeService::getServiceId, req.getKeyword())
                                .or().like(MemberSubscribeService::getName, req.getKeyword()))
                .orderByAsc(MemberSubscribeService::getServiceId)
                .page(new Page<>(1, 100, false));
        return bindSelectOptions(page.getRecords(), MemberSubscribeService::getServiceId,
                MemberSubscribeService::getName);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.CONFIG_VIEW)
    @GetMapping("/detail/{id}")
    public R<MemberSubscribeService> detail(@PathVariable("id") String id) {
        MemberSubscribeService entity = service.getById(id);
        Assert.notNull(entity, () ->  CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(id));
        return R.ok(entity);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.CREATE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_ADD)
    @Log(title = "会员订阅服务项", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<Void> create(@Valid @RequestBody SaveMemberSubscribeServiceReq req) {
        service.createMemberSubscribeService(req);
        return R.ok();
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_EDIT)
    @Log(title = "会员订阅服务项", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody SaveMemberSubscribeServiceReq req) {
        service.updateMemberSubscribeService(req);
        return R.ok();
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.DELETE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_REMOVE)
    @Log(title = "会员订阅服务项", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @NotEmpty List<@NotNull String> ids) {
        service.deleteMemberSubscribeServices(ids);
        return R.ok();
    }
}
