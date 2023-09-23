package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class MemberInfoDTO {

    @NotBlank
    private String nickName;

    private String slogan;

    private String description;
}
