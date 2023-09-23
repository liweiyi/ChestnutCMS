package com.chestnut.contentcore.properties;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.config.SiteApiUrl;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 站点API域名地址
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + SiteApiUrlProperty.ID)
public class SiteApiUrlProperty implements IProperty {

	public final static String ID = "SiteApiUrl";
	
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
		return "站点API域名地址";
	}
	
	@Override
	public String defaultValue() {
		return SiteApiUrl.getValue();
	}
	
	@Override
	public String getPropValue(Map<String, String> configProps) {
		String propValue = MapUtils.getString(configProps, getId());
		if (StringUtils.isNotEmpty(propValue)) {
			return propValue;
		}
		return defaultValue();
	}
	
	public static String getValue(CmsSite site, String publishPipeCode) {
		String apiUrl = ConfigPropertyUtils.getStringValue(ID, site.getConfigProps());
		if (StringUtils.isEmpty(apiUrl)) {
			apiUrl = SiteApiUrl.getValue();
		}
		if (StringUtils.isEmpty(apiUrl)) {
			apiUrl = site.getUrl(publishPipeCode);
		}
		if (StringUtils.isNotEmpty(apiUrl) && !apiUrl.endsWith("/")) {
			apiUrl += "/";
		}
		return apiUrl;
	}
}
