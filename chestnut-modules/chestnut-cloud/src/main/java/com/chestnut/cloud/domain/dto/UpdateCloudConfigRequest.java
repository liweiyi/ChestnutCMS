package com.chestnut.cloud.domain.dto;

import com.chestnut.system.domain.dto.CreateLoginConfigRequest;
import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

/**
 * UpdateCloudConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class UpdateCloudConfigRequest extends CreateLoginConfigRequest {

    @LongId
    private Long configId;
}
