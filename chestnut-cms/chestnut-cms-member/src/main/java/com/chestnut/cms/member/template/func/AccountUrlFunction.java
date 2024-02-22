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
package com.chestnut.cms.member.template.func;

import com.chestnut.cms.member.utils.CmsMemberUtils;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：清理html标签
 */
@Component
@RequiredArgsConstructor
public class AccountUrlFunction extends AbstractFunc  {

	private static final String FUNC_NAME = "accountUrl";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 2 || ObjectUtils.isAnyNull(args)) {
			return StringUtils.EMPTY;
		}
		boolean includeBaseArgs = true;
		if (args.length > 2) {
			includeBaseArgs = ((TemplateBooleanModel) args[2]).getAsBoolean();
		}
		long memberId;
		if (args[0] instanceof  TemplateNumberModel tnm) {
			memberId = tnm.getAsNumber().longValue();
		} else {
			memberId = Long.parseLong(args[0].toString());
		}
		String type = args[1].toString();
		if (!IdUtils.validate(memberId)) {
			throw  new TemplateModelException("Can't use `accountUrl` function without Member.memberId.");
		}
		return CmsMemberUtils.getAccountUrl(memberId, type, Environment.getCurrentEnvironment(), includeBaseArgs);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("会员ID", FuncArgType.Long, true, null),
				new FuncArg("类型", FuncArgType.String, true, "用于处理会员主页展示数据，可在模板中根据类型显示会员评论、收藏等数据。"),
				new FuncArg("是否带站点ID和发布通道参数", FuncArgType.String, false, "默认：true"));
	}
}
