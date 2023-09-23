package com.chestnut.cms.stat.properties;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 百度统计域名
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + BaiduTjDomainProperty.ID)
public class BaiduTjDomainProperty implements IProperty {

	public final static String ID = "BaiduTjDomain";

	static UseType[] UseTypes = new UseType[] { UseType.Site };

	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "百度统计域名";
	}

	public static String[] getValue(Map<String, String> siteConfigProps) {
		String value = ConfigPropertyUtils.getStringValue(ID, siteConfigProps);
		return Objects.isNull(value) ? ArrayUtils.EMPTY_STRING_ARRAY : StringUtils.split(value, ",");
	}
}
