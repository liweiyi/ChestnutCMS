package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * 会员登录DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@Validated
public class MemberLoginDTO {

    /**
     * 登录方式（Email, 手机号， 手机验证码，用户名）
     */
    @NotEmpty
    private String type;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String code;

    private String uuid;

    private String ip;

    private String userAgent;
}
