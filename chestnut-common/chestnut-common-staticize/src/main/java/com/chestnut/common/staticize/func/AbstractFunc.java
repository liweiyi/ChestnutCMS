package com.chestnut.common.staticize.func;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public abstract class AbstractFunc implements IFunction, TemplateMethodModelEx {
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object exec(List args) throws TemplateModelException {
		return exec0(args.toArray());
	}
	
	public abstract Object exec0(Object... args) throws TemplateModelException;
}
