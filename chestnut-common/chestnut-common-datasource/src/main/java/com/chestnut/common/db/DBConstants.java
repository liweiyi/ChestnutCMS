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
package com.chestnut.common.db;

/**
 * DB 公用变量
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class DBConstants {

    /**
     * 逻辑删除标识：未删除
     */
    public static final int DELETED_NO = 0;

    /**
     * 逻辑删除标识：已删除
     */
    public static final int DELETED_YES = 1;

    public static boolean isDeleted(Integer deleted) {
        return deleted != null && deleted.intValue() == DELETED_YES;
    }
}
