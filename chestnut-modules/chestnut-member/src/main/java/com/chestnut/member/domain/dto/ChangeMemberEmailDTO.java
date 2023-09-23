package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * 修改会员绑定邮箱
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ChangeMemberEmailDTO {

    @NotEmpty
    private String authCode;

    @NotEmpty
    @Email
    private String email;
}
