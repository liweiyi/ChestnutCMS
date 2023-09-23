package com.chestnut.system.domain;

import org.hibernate.validator.constraints.Length;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 国际化翻译字典表 sys_i18n_dict
 * 
 * <p>
 * 适用字符长度不超过100的字段，在表字段添加@I18nFied注解，查询数据后根据此表自动转换成当前语言环境字符
 * </p>
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysI18nDict.TABLE_NAME)
public class SysI18nDict {

	public static final String TABLE_NAME = "sys_i18n_dict";

	@TableId(value = "dict_id", type = IdType.INPUT)
	private Long dictId;
	
	/**
	 * 各国语言环境简称字符串，例如：zh-CN=中文（简体），en-US=英文（美国）
	 */
	@NotBlank
	@Length(max = 10)
	private String langTag;

	/**
	 * 需要翻译的字符串唯一标识
	 */
	@NotBlank
	@Length(max = 100)
	private String langKey;

	/**
	 * 当前语言黄精langId对应的翻译
	 */
	@NotBlank
	@Length(max = 100)
	private String langValue;
}
