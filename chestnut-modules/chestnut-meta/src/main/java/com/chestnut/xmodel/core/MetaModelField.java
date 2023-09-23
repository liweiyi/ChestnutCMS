package com.chestnut.xmodel.core;

import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.FieldOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MetaModelField {

    private String name;

    private String code;

    private String fieldName;

    private Object fieldValue;

    private boolean editable;

    private String defaultValue;

    private boolean primaryKey;

    private boolean mandatory;

    private String controlType;

    private FieldOptions options;

    public MetaModelField(String name, String code, String fieldName,
                          boolean primaryKey, boolean mandatory, String controlType) {
        this.name = name;
        this.code = code;
        this.fieldName = fieldName;
        this.primaryKey = primaryKey;
        this.mandatory = mandatory;
        this.controlType = controlType;
    }

    public MetaModelField(XModelField field) {
        this.name = field.getName();
        this.code = field.getCode();
        this.fieldName = field.getFieldName();
        this.mandatory = YesOrNo.isYes(field.getMandatoryFlag());
        this.controlType = field.getControlType();
        this.defaultValue = field.getDefaultValue();
        this.options = field.getOptions();
        this.editable = true;
    }
}