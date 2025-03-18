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
package com.chestnut.cms.vote.exception;

import com.chestnut.common.i18n.I18nUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;

/**
 * VoteNotFoundTemplateException
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class VoteNotFoundTemplateException extends TemplateException {

    public VoteNotFoundTemplateException(String code, Environment env) {
        super(I18nUtils.get("FREEMARKER.ERR.VOTE_NOT_FOUND", code), env);
    }
}
