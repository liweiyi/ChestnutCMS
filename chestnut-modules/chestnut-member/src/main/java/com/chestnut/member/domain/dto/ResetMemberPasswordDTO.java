package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改会员绑定邮箱
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ResetMemberPasswordDTO {

    @NotEmpty
    private String password;

    @NotEmpty
    private String newPassword;
}
