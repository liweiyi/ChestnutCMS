package com.chestnut.xmodel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 元数据模型对象 [XModel]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(XModel.TABLE_NAME)
public class XModel extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "x_model";

    @TableId(value = "model_id", type = IdType.INPUT)
    private Long modelId;

    /**
     * 类型
     */
    private String ownerType;

    /**
     * 类型关联ID
     */
    private String ownerId;

    /**
     * 名称
     */
    private String name;

    /**
     * 唯一标识编码
     */
    private String code;

    /**
     * 数据保存表
     */
    private String tableName;

    @TableField(exist = false)
    private Boolean isDefaultTable;
}
