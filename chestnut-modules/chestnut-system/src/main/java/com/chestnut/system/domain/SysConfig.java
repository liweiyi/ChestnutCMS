package com.chestnut.system.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.i18n.I18nField;
import com.chestnut.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 参数配置表 sys_config
 */
@Getter
@Setter
@TableName(SysConfig.TABLE_NAME)
public class SysConfig extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_config";

	/** 参数主键 */
	@ExcelProperty("参数主键")
	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;

	/** 参数名称 */
	@ExcelProperty("参数名称")
	@I18nField("{CONFIG.#{configKey}}")
	private String configName;

	/** 参数键名 */
	@ExcelProperty("参数键名")
	private String configKey;

	/** 参数键值 */
	@ExcelProperty("参数键值")
	private String configValue;

	/**
	 * 是否系统固定配置参数
	 */
	@TableField(exist = false)
	private String fixed;

	@NotBlank(message = "参数名称不能为空")
	@Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
	public String getConfigName() {
		return configName;
	}

	@NotBlank(message = "参数键名长度不能为空")
	@Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
	public String getConfigKey() {
		return configKey;
	}

	@NotBlank(message = "参数键值不能为空")
	@Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
	public String getConfigValue() {
		return configValue;
	}
}
