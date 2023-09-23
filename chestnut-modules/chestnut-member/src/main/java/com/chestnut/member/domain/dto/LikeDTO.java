package com.chestnut.member.domain.dto;

import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LikeDTO {

    @NotEmpty
    private String dataType;

    @LongId
    private Long dataId;
}
