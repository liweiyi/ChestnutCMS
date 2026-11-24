package com.chestnut.member.domain.dto;

import com.chestnut.common.annotation.XComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询会员订阅服务项及已选服务项选项。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.QUERY_REQUEST}")
public class QueryMemberSubscribeServiceReq {

    /**
     * 服务项ID或名称查询
     */
    @Size(max = 100, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.KEYWORD}")
    @XComment("{API.DOC.MEMBER_SUBSCRIBE_SERVICE.KEYWORD}")
    private String keyword;

    @Size(max = 100, message = "{VALID.MEMBER_SUBSCRIPTION.INVALID}")
    @XComment("{ENT.MEMBER_SUBSCRIPTION.SERVICE_IDS}")
    private List<@NotBlank(message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.ID}")
            @Size(max = 100, message = "{VALID.MEMBER_SUBSCRIBE_SERVICE.ID}") String> ids;
}
