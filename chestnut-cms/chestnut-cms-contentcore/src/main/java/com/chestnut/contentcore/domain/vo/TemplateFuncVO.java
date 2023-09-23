package com.chestnut.contentcore.domain.vo;

import java.util.List;

import com.chestnut.common.staticize.func.IFunction.FuncArg;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TemplateFuncVO {
	
	private String funcName;
	
	private String desc;
	
	private List<FuncArg> funcArgs;
}
