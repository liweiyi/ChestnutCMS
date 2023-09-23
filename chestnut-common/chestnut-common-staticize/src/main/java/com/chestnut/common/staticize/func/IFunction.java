package com.chestnut.common.staticize.func;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public interface IFunction {
	
	/**
	 * 函数使用名<br/>
	 * 例如：${replace(txt, 'findStr', 'repStr')}
	 * 
	 * @return
	 */
	public String getFuncName();
	
	/**
	 * 描述
	 * 
	 * @return
	 */
	public String getDesc();
	
	/**
	 * 获取函数参数定义列表
	 * 
	 * @return
	 */
	public List<FuncArg> getFuncArgs();

	/**
	 * 模板函数参数
	 */
	@Getter
	@Setter
	public class FuncArg {
		
		private String name;
		
		private FuncArgType type;
		
		private boolean required;
		
		private String desc;
		
		public FuncArg(String name, FuncArgType type, boolean required, String desc) {
			this.name = name;
			this.type = type;
			this.required = required;
			this.desc = desc;
		}
	}
	
	public enum FuncArgType {
		String, Int, Long, Float, Double, DateTime, Boolean
	}
}
