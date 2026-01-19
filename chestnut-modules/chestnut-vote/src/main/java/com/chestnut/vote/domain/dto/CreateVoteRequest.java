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
package com.chestnut.vote.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.system.validator.Dict;
import com.chestnut.vote.fixed.dict.VoteStatus;
import com.chestnut.vote.fixed.dict.VoteViewType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * CreateVoteRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateVoteRequest extends BaseDTO {

    /**
     * 唯一标识编码
     */
    @NotBlank
    @Length(max = 20)
    @Pattern(regexp = RegexConsts.REGEX_CODE)
    private String code;

    /**
     * 问卷调查标题
     */
    @NotBlank
    @Length(max = 255)
    private String title;

    /**
     * 开始时间
     */
    @NotNull
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull
    private LocalDateTime endTime;

    /**
     * 用户类型（IP、浏览器指纹，登录用户）
     */
    @NotBlank
    @Length(max = 20)
    private String userType;

    /**
     * 每日限制次数
     */
    @NotNull
    @Min(1)
    private Integer dayLimit;

    /**
     * 总共可参与次数
     */
    @NotNull
    @Min(1)
    private Integer totalLimit;

    /**
     * 状态
     */
    @NotBlank
    @Dict(VoteStatus.TYPE)
    private String status;

    /**
     * 结果查看方式（不允许查看、提交后可看、不限制）
     */
    @NotBlank
    @Dict(VoteViewType.TYPE)
    private String viewType;

    private String source;

    @Length(max = 500)
    private String remark;
}
