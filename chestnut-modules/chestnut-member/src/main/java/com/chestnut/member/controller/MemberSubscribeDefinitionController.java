package com.chestnut.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.core.IMemberSubscribeServiceItem;
import com.chestnut.member.domain.MemberSubscribeDefinition;
import com.chestnut.member.domain.dto.ChangeSubscribeDefinitionStatusReq;
import com.chestnut.member.domain.dto.CreateSubscribeDefinitionReq;
import com.chestnut.member.domain.dto.QuerySubscribeDefinitionReq;
import com.chestnut.member.domain.dto.UpdateSubscribeDefinitionReq;
import com.chestnut.member.domain.vo.SubscriptionServiceItemVO;
import com.chestnut.member.exception.MemberSubscribeErrorCode;
import com.chestnut.member.permission.MemberSubscribePriv;
import com.chestnut.member.service.IMemberSubscribeDefinitionService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员订阅定义及业务服务项管理入口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.MEMBER_SUBSCRIPTION.DEFINITION_MODULE}")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/member/subscription/definition")
public class MemberSubscribeDefinitionController extends BaseRestController {

    private final IMemberSubscribeDefinitionService definitionService;

    private final List<IMemberSubscribeServiceItem> serviceItems;

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.LIST}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.CONFIG_VIEW)
    @GetMapping("/list")
    public R<TableData<MemberSubscribeDefinition>> list(
            @ModelAttribute @Validated QuerySubscribeDefinitionReq req) {
        PageRequest pr = getPageRequest();
        Page<MemberSubscribeDefinition> page =
                this.definitionService.lambdaQuery()
                        .like(StringUtils.isNotBlank(req.getSubscriptionName()), MemberSubscribeDefinition::getSubscriptionName,
                                req.getSubscriptionName())
                        .orderByDesc(MemberSubscribeDefinition::getCreateTime)
                        .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.CONFIG_VIEW)
    @GetMapping("/detail/{subscriptionId}")
    public R<MemberSubscribeDefinition> detail(
            @PathVariable
            @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
            @Size(max = 64, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
            @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
            String subscriptionId
    ) {
        MemberSubscribeDefinition definition = definitionService.getById(subscriptionId);
        Assert.notNull(definition, MemberSubscribeErrorCode.SUBSCRIPTION_NOT_FOUND::exception);
        return R.ok(definition);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.ITEMS}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/serviceItems")
    public R<List<SubscriptionServiceItemVO>> serviceItems() {
        List<SubscriptionServiceItemVO> list =
                serviceItems.stream()
                        .map(item -> new SubscriptionServiceItemVO(
                            item.getServiceId(),
                            I18nUtils.get(item.getName()),
                            I18nUtils.get(item.getDescription()))
                        ).toList();
        return R.ok(list);
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.OPTIONS}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/options")
    public R<List<SelectOption>> options(
            @RequestParam(required = false)
            @Size(max = 128, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
            @XComment("{API.DOC.MEMBER_SUBSCRIPTION.KEYWORD}")
            String keyword,
            @RequestParam(required = false)
            @Size(max = 100, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
            @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
            List<String> ids) {
        Page<MemberSubscribeDefinition> page = definitionService.lambdaQuery()
                .select(MemberSubscribeDefinition::getDefinitionId, MemberSubscribeDefinition::getSubscriptionName)
                .in(StringUtils.isNotEmpty(ids), MemberSubscribeDefinition::getDefinitionId, ids)
                .like(StringUtils.isNotBlank(keyword), MemberSubscribeDefinition::getSubscriptionName, keyword)
                .orderByDesc(MemberSubscribeDefinition::getCreateTime)
                .page(new Page<>(1, 100, false));
        return bindSelectOptions(
                page.getRecords(),
                MemberSubscribeDefinition::getDefinitionId,
                MemberSubscribeDefinition::getSubscriptionName
        );
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.CREATE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_ADD)
    @Log(title = "MemberSubscriptionDef", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated CreateSubscribeDefinitionReq req) {
        definitionService.createDefinition(req);
        return R.ok();
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_EDIT)
    @Log(title = "MemberSubscriptionDef", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated UpdateSubscribeDefinitionReq req) {
        definitionService.updateDefinition(req);
        return R.ok();
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.CHANGE_STATUS}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_EDIT)
    @Log(title = "MemberSubscriptionDef", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody @Validated ChangeSubscribeDefinitionStatusReq req) {
        definitionService.changeDefinitionStatus(req);
        return R.ok();
    }

    @XComment("{API.DOC.MEMBER_SUBSCRIPTION.DELETE}")
    @Priv(type = AdminUserType.TYPE, value = MemberSubscribePriv.DEFINITION_REMOVE)
    @Log(title = "会员订阅定义", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public R<Void> delete(
            @NotBlank(message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
            @XComment("{ENT.MEMBER_SUBSCRIPTION.SUBSCRIPTION_ID}")
            @RequestParam("definitionId")
            String definitionId
    ) {
        definitionService.deleteDefinition(definitionId, StpAdminUtil.getLoginUser());
        return R.ok();
    }
}
