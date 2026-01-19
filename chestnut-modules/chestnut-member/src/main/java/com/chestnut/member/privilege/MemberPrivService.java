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
package com.chestnut.member.privilege;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * MemberPrivService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberPrivService {



    /**
     * 保存会员权限信息+
     *
     * @param ownerType
     * @param owner
     */
    public void savePrivilege(String ownerType, String owner, String privType, Set<String> privKeys) {

    }
}
