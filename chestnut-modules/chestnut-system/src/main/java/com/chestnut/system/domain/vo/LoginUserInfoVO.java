/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chestnut.common.annotation.XComment;
import com.chestnut.system.annotation.ExcelDictField;
import com.chestnut.system.config.converter.DictConverter;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * LoginUserInfoVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LoginUserInfoVO {

    private LoginUserData user;

    private List<String> roles;

    private List<String> permissions;

    public void setUser(SysUser sysUser) {
        this.user = new LoginUserData();
        BeanUtils.copyProperties(sysUser, user);
    }

    @Getter
    @Setter
    public static class LoginUserData {
        @XComment("{ENT.SYS.USER.ID}")
        @ExcelProperty(value = "{ENT.SYS.USER.ID}" ,converter = LongStringConverter.class)
        @TableId(value = "user_id", type = IdType.INPUT)
        private Long userId;

        /** 部门ID */
        @ExcelIgnore
        @XComment("{ENT.SYS.USER.DEPT_ID}")
        private Long deptId;

        @ExcelProperty("{ENT.SYS.USER.DEPT_NAME}")
        @TableField(exist = false)
        private String deptName;

        /** 用户账号 */
        @ExcelProperty("{ENT.SYS.USER.USER_NAME}")
        @NotBlank
        @Length(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9_]+$")
        private String userName;

        /** 用户昵称 */
        @ExcelProperty("{ENT.SYS.USER.NICK_NAME}")
        @Length(max = 30)
        private String nickName;

        /** 真实姓名 */
        @ExcelProperty("{ENT.SYS.USER.REAL_NAME}")
        private String realName;

        /** 用户邮箱 */
        @ExcelProperty("{ENT.SYS.USER.MAIL}")
        @Email
        @Length(max = 50)
        private String email;

        /** 手机号码 */
        @ExcelProperty("{ENT.SYS.USER.PHONE}")
        @Length(max = 11)
        private String phoneNumber;

        /** 用户性别 */
        @ExcelProperty(value = "{ENT.SYS.USER.GENDER}", converter = DictConverter.class)
        @ExcelDictField(Gender.TYPE)
        @Dict(value = Gender.TYPE)
        private String sex;

        /** 出生日期 */
        @ExcelProperty("{ENT.SYS.USER.BIRTHDAY}")
        private LocalDateTime birthday;

        @ExcelProperty("{ENT.SYS.USER.LOGIN_IP}")
        private String avatar;

        @ExcelProperty("{ENT.SYS.USER.LOGIN_IP}")
        private String avatarSrc;

        @ExcelProperty("{ENT.SYS.USER.LOGIN_IP}")
        private String loginIp;

        @XComment(value = "{ENT.SYS.USER.LOGIN_TIME}")
        private LocalDateTime loginDate;

        @XComment(value = "{ENT.SYS.USER.PWD_MODIFY_TIME}")
        private LocalDateTime passwordModifyTime;

        @XComment("{ENT.SYS.USER.FORCE_MODIFY_PWD}")
        private String forceModifyPassword;

        @XComment("{ENT.SYS.USER.LOCK_END_TIME}")
        private LocalDateTime lockEndTime;

        @XComment("{ENT.SYS.USER.PREFERENCES}")
        private Map<String, Object> preferences;
        /**
         * 密码是否过期
         */
        private Boolean isPasswordExpired = false;
    }
}
