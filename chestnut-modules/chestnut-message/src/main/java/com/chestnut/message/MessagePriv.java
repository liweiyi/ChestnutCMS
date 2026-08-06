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
package com.chestnut.message;

public interface MessagePriv {

    String CONFIG_VIEW = "message:config:view";

    String CONFIG_ADD = "message:config:add";

    String CONFIG_EDIT = "message:config:edit";

    String CONFIG_DELETE = "message:config:del";

    String TEMPLATE_VIEW = "message:template:view";

    String TEMPLATE_ADD = "message:template:add";

    String TEMPLATE_EDIT = "message:template:edit";

    String TEMPLATE_DELETE = "message:template:del";

    String EVENT_VIEW = "message:event:view";

    String EVENT_SAVE = "message:event:save";

    String EVENT_DELETE = "message:event:del";
}
