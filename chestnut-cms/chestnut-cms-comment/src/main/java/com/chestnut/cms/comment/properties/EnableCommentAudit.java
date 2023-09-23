package com.chestnut.cms.comment.properties;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 评论是否需要审核
 */
@Component(IProperty.BEAN_NAME_PREFIX + EnableCommentAudit.ID)
public class EnableCommentAudit implements IProperty {

	public final static String ID = "EnableCommentAudit";
	
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
		return "评论是否需要审核";
	}

	@Override
	public String defaultValue() {
		return YesOrNo.YES;
	}

	public static boolean getValue(Map<String, String> props) {
		String v = ConfigPropertyUtils.getStringValue(ID, props);
		if (StringUtils.isEmpty(v)) {
			v = YesOrNo.YES;
		}
		return YesOrNo.isYes(v);
	}
}
