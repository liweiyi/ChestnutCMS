package com.chestnut.common.db.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DBTableColumn {

    /**
     * 字段名称
     */
    private String label;

    /**
     * 字段名称（COLUMN_NAME）
     */
    private String name;

    /**
     * 字段类型（DATA_TYPE）
     * 来自 java.sql.Types 的 SQL 类型
     */
    private int type;

    /**
     * 字段类型名称（TYPE_NAME）
     */
    private String typeName;

    /**
     * 列大小
     */
    private int size;

    /**
     * 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null
     */
    private int decimalDigits;

    /**
     * 基数（通常为 10 或 2）
     */
    private int numPrecRadix;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    /**
     * 是否主键
     */
    private boolean primary;

    /**
     * 是否可为空
     */
    private boolean nullable;

    /**
     * 字段注释
     */
    private String comment;

    public DBTableColumn() {

    }

    public DBTableColumn(String name, int type, String defaultValue, Boolean nullable,
             Boolean primary, Boolean autoIncrement) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.nullable = nullable;
        this.primary = primary;
        this.autoIncrement = autoIncrement;
    }
}
