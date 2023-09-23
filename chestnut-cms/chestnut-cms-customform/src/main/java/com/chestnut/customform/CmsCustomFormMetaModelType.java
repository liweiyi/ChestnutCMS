package com.chestnut.customform;

import com.chestnut.customform.domain.CmsCustomFormData;
import com.chestnut.exmodel.domain.CmsExtendModelData;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.core.impl.MetaControlType_Input;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元数据模型类型：自定义表单
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaModelType.BEAN_PREFIX + CmsCustomFormMetaModelType.TYPE)
public class CmsCustomFormMetaModelType implements IMetaModelType {

    public final static String TYPE = "CmsCustomForm";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTableNamePrefix() {
        return "cms_cfd_";
    }

    @Override
    public String getDefaultTable() {
        return CmsCustomFormData.TABLE_NAME;
    }

    @Override
    public List<MetaModelField> getFixedFields() {
        return List.of(FIELD_DATA_ID, FIELD_SITE_ID, FIELD_MODEL_ID, FIELD_CLIENT_IP, FIELD_UUID, FIELD_CREATE_TIME);
    }

    /**
     * 数据表必须包含的字段
     */
    public static final MetaModelField FIELD_MODEL_ID = new MetaModelField("模型ID", "modelId",
            MODEL_ID_FIELD_NAME, false, true, MetaControlType_Input.TYPE);

    public static final MetaModelField FIELD_DATA_ID = new MetaModelField("数据ID", "dataId",
            "data_id", true, true, MetaControlType_Input.TYPE);
    public static final MetaModelField FIELD_SITE_ID = new MetaModelField("站点ID", "siteId",
            "site_id", false, true, MetaControlType_Input.TYPE);
    public static final MetaModelField FIELD_CLIENT_IP = new MetaModelField("IP", "clientIp",
            "client_ip", false, true, MetaControlType_Input.TYPE);
    public static final MetaModelField FIELD_UUID = new MetaModelField("UUID", "uuid",
            "uuid", false, false, MetaControlType_Input.TYPE);
    public static final MetaModelField FIELD_CREATE_TIME = new MetaModelField("创建时间", "createTime",
            "create_time", false, true, MetaControlType_Input.TYPE);
}
