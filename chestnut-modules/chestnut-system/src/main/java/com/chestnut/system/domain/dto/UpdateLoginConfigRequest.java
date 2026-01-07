package com.chestnut.system.domain.dto;

import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

/**
 * UpdateLoginConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class UpdateLoginConfigRequest extends CreateLoginConfigRequest {

    @LongId
    private Long configId;
}
