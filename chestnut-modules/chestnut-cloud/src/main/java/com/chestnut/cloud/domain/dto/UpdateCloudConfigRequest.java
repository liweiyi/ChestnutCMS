package com.chestnut.cloud.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

/**
 * UpdateCloudConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CLOUD.UPDATE_REQ}")
@Getter
@Setter
public class UpdateCloudConfigRequest extends CreateCloudConfigRequest {

    @XComment("{API.DOC.CLOUD.CONFIG_ID}")
    @LongId
    private Long configId;
}
