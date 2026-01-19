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
package com.chestnut.common.db.domain;

import java.time.LocalDateTime;

/**
 * 备份表Entity接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IBackupEntity<T> {

    T toSourceEntity();

    Long getBackupId();

    void setBackupId(Long backupId);

    String getBackupBy();

    void setBackupBy(String backupBy);

    LocalDateTime getBackupTime();

    void setBackupTime(LocalDateTime backupTime);

    String getBackupRemark();

    void setBackupRemark(String backupRemark);
}
