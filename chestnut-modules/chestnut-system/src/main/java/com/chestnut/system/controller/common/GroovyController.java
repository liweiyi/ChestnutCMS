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
package com.chestnut.system.controller.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysGroovyScript;
import com.chestnut.system.domain.dto.ExecGroovyScriptRequest;
import com.chestnut.system.domain.dto.SaveGroovyScriptRequest;
import com.chestnut.system.groovy.BaseGroovyScript;
import com.chestnut.system.groovy.GroovyScriptFactory;
import com.chestnut.system.mapper.SysGroovyScriptMapper;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Groovy脚本执行控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.GroovyExec)
@RestController
@RequiredArgsConstructor
@RequestMapping("/groovy")
public class GroovyController extends BaseRestController {

	private final SysGroovyScriptMapper groovyScriptMapper;

	@Log(title = "执行Groovy脚本", businessType = BusinessType.UPDATE)
	@PostMapping("/exec")
	public R<?> execGroovyScript(@RequestBody @Validated ExecGroovyScriptRequest dto) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		try {
			BaseGroovyScript script = GroovyScriptFactory.getInstance().loadNewInstance(dto.getScriptText());
			script.setPrintWriter(printWriter);
			script.run();
		} catch (Exception e) {
			e.printStackTrace(printWriter);
		}
		return R.ok(writer.toString());
	}

	@GetMapping("/list")
	public R<?> getGroovyScripts() {
		List<SysGroovyScript> groovyScripts = groovyScriptMapper.selectList(new LambdaQueryWrapper<>());
		return bindDataTable(groovyScripts);
	}

	@Log(title = "保存Groovy脚本", businessType = BusinessType.INSERT)
	@PostMapping("/save")
	public R<?> saveGroovyScript(@RequestBody @Validated SaveGroovyScriptRequest dto) {
		SysGroovyScript groovyScript = new SysGroovyScript();
		groovyScript.setScriptId(IdUtils.getSnowflakeId());
		groovyScript.setName(dto.getName());
		groovyScript.setScriptText(dto.getScriptText());
		groovyScript.setRemark(dto.getRemark());
		groovyScript.createBy(StpAdminUtil.getLoginUser().getUsername());
		groovyScriptMapper.insert(groovyScript);
		return R.ok();
	}

	@Log(title = "刪除Groovy脚本", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> deleteGroovyScript(@RequestBody @NotEmpty List<Long> scriptIds) {
		this.groovyScriptMapper.deleteByIds(scriptIds);
		return R.ok();
	}

	@GetMapping("/{scriptId}")
	public R<?> getGroovyScript(@PathVariable @LongId Long scriptId) {
		SysGroovyScript groovyScript = this.groovyScriptMapper.selectById(scriptId);
		Assert.notNull(groovyScript, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("scriptId", scriptId));
		return R.ok(groovyScript);
	}
}
