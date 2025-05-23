/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.member.util;

import com.chestnut.member.config.MemberConfig;
import com.chestnut.member.fixed.config.MemberResourcePrefix;
import com.chestnut.system.fixed.config.BackendContext;

/**
 * 会员工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MemberUtils {

    /**
     * 获取会员资源文件访问前缀
     *
     * @param isPreview 是否预览模式
     */
    public static String getMemberResourcePrefix(boolean isPreview) {
        if (isPreview) {
            return BackendContext.getValue() + MemberConfig.getResourcePrefix();
        }
        return MemberResourcePrefix.getValue();
    }
}
