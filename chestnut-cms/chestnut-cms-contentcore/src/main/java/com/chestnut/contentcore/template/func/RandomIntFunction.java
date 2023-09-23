package com.chestnut.contentcore.template.func;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：随机数
 */
@Component
@RequiredArgsConstructor
public class RandomIntFunction extends AbstractFunc {

	static final String FUNC_NAME = "randomInt";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private static final Random random = new Random();

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
		if (args.length < 1) {
			return StringUtils.EMPTY;
		}
		int origin = ((SimpleNumber) args[0]).getAsNumber().intValue();
		if (args.length == 1) {
			return random.nextInt(origin);
		} else if (args.length == 2) {
			int bound = ((SimpleNumber) args[1]).getAsNumber().intValue();
			return random.nextInt(origin, bound);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("随机数范围最（大/小）值", FuncArgType.Int, true, "只有1个参数时：[0, arg[0])，2个参数时：[arg[0], arg[1])"),
				new FuncArg("随机数范围最大值", FuncArgType.Int, false, null));
	}
}
