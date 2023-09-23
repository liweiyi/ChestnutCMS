package com.chestnut.member.domain.dto;

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
public class UploadMemberAvatarDTO {

    @NotEmpty
    private String image;
}
