package com.chestnut.common.staticize.tag;

import java.util.List;

public interface ITag {
	
    /**
     * 标签名
     * 
     * <@value></@value>
     */
    public String getTagName();

    /**
     * 标签名称
     */
    public String getName();

    /**
     * 标签描述
     */
    default public String getDescription() {
        return "";
    }
    
    /**
     * 标签属性定义
     */
    default public List<TagAttr> getTagAttrs() {
    	return null;
    }
}
