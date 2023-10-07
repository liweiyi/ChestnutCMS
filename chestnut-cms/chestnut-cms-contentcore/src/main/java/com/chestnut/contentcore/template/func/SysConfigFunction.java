package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.service.ISysConfigService;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：获取系统参数指定Key数据
 */
@Component
@RequiredArgsConstructor
public class SysConfigFunction extends AbstractFunc {

	private static final String FUNC_NAME = "sysConfig";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private final ISysConfigService sysConfigService;

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
		if (args.length != 1) {
			return null;
		}
		String configKey = args[0].toString();
		if (StringUtils.isBlank(configKey)) {
			return null;
		}
		return this.sysConfigService.selectConfigByKey(configKey);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("参数键名", FuncArgType.String, true, null));
	}
}
