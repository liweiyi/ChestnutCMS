/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.member.listener.event;

import com.chestnut.member.domain.MemberFavorites;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会员取消收藏后事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class AfterMemberCancelFavoriteEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private MemberFavorites memberFavorites;

    public AfterMemberCancelFavoriteEvent(Object source, MemberFavorites memberFavorites) {
        super(source);
        this.memberFavorites = memberFavorites;
    }
}
