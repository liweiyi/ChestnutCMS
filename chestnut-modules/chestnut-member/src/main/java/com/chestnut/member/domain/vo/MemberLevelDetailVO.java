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
package com.chestnut.member.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.domain.MemberLevelConfig;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.LEVEL_DETAIL}")
public class MemberLevelDetailVO {

    @XComment("{API.DOC.MEMBER.LEVEL_TYPE}")
    private String levelType;

    @XComment("{API.DOC.MEMBER.LEVEL_TYPE_NAME}")
    private String levelTypeName;

    @XComment("{API.DOC.MEMBER.LEVEL}")
    private Integer level;

    @XComment("{API.DOC.MEMBER.LEVEL_NAME}")
    private String name;

    @XComment("{API.DOC.MEMBER.LEVEL_CURRENT_EXP}")
    private Long exp;

    @XComment("{API.DOC.MEMBER.LEVEL_NEXT_NEED_EXP}")
    private Long nextNeedExp;

    @XComment("{API.DOC.MEMBER.LEVEL_PROGRESS}")
    private Double progress;

    @XComment("{API.DOC.MEMBER.LEVEL_MAX}")
    private boolean maxLevel;

    public static MemberLevelDetailVO from(String type, String typeName, MemberLevel data, List<MemberLevelConfig> configs) {
        MemberLevelDetailVO vo = new MemberLevelDetailVO();
        vo.setLevelType(type);
        vo.setLevelTypeName(typeName);
        vo.setLevel(data == null ? 0 : data.getLevel());
        vo.setExp(data == null ? 0L : data.getExp());
        MemberLevelConfig current = configs.stream().filter(c -> c.getLevel().equals(vo.getLevel())).findFirst().orElse(null);
        if (current == null) return vo;
        vo.setName(current.getName());
        vo.setMaxLevel(configs.stream().noneMatch(c -> c.getLevel() > vo.getLevel()));
        if (vo.isMaxLevel()) {
            vo.setProgress(100D);
        } else if (current.getNextNeedExp() != null && current.getNextNeedExp() > 0) {
            vo.setNextNeedExp(current.getNextNeedExp());
            // Experience is the amount remaining in the current level, rather than lifetime experience.
            vo.setProgress(BigDecimal.valueOf(vo.getExp()).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(vo.getNextNeedExp()), 2, RoundingMode.HALF_UP)
                    .max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).doubleValue());
        }
        return vo;
    }
}
