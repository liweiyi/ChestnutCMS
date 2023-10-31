package com.chestnut.cms.search.fixed.config;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.fixed.dict.WordAnalyzeType;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 内容索引分词策略
 */
@Component(FixedConfig.BEAN_PREFIX + SearchAnalyzeType.ID)
public class SearchAnalyzeType extends FixedConfig {

	public static final String ID = "CMSSearchAnalyzeType";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	public static final List<String> ALLOWED_LIST = List.of(
			WordAnalyzeType.IKMaxWord,
			WordAnalyzeType.IKSmart
	);

	public SearchAnalyzeType() {
		super(ID, "{CONFIG." + ID + "}", WordAnalyzeType.IKSmart,"支持：ik_smart/ik_max_word");
	}

	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue) || !ALLOWED_LIST.contains(configValue)) {
			return WordAnalyzeType.IKSmart;
		}
		return configValue;
	}
}
