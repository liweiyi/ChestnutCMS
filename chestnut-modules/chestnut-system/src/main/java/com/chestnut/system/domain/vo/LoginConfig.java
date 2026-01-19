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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * LoginConfig
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LoginConfig {

    private CaptchaConfig captcha = new CaptchaConfig();

    private List<ThirdLogin> thirds;

    @Getter
    @Setter
    public static class ThirdLogin {
        private Long id;
        private String type;
    }

    @Getter
    @Setter
    public static class CaptchaConfig {
        private boolean enabled = false;
        private String type = "";
        private int expires = 0;
        private int duration = 0;
    }
}
