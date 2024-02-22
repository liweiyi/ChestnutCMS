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
package com.chestnut.system.controller.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.system.groovy.BaseGroovyScript;
import com.chestnut.system.groovy.GroovyScriptFactory;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Groovy脚本执行控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/groovy")
public class GroovyController {

	@Log(title = "执行Groovy脚本", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.GroovyExec)
	@PostMapping("/exec")
	public R<?> execGroovyScript(@RequestBody @Validated ScriptBody scriptBody) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		try {
			BaseGroovyScript script = GroovyScriptFactory.getInstance().loadNewInstance(scriptBody.getScriptText());
			script.setPrintWriter(printWriter);
			script.run();
		} catch (Exception e) {
			e.printStackTrace(printWriter);
		}
		return R.ok(writer.toString());
	}
	
	@Getter
	@Setter
	static class ScriptBody {
		
		@NotEmpty
		private String scriptText;
	}
}
