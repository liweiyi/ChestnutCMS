package com.chestnut.common.staticize.tag;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签属性可选项
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagAttrOption {

    /**
     * 标签属性可选项值
     */
    String value;

    /**
     * 标签属性可选项描述
     */
    String desc;

    public TagAttrOption(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String lowerCaseValue() {
        return this.value.toLowerCase();
    }
}
