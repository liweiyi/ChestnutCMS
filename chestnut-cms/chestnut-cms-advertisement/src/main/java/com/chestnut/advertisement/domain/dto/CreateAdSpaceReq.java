package com.chestnut.advertisement.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CreateAdSpaceReq
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.AD_SPACE.CREATE_REQ}")
@Getter
@Setter
public class CreateAdSpaceReq extends BaseDTO {

    @NotEmpty
    @XComment("{CMS.AD_SPACE.NAME}")
    private String name;

    @NotEmpty
    @XComment("{CMS.AD_SPACE.CODE}")
    private String code;

    @Deprecated(since = "1.5.6", forRemoval = true)
    private String publishPipeCode;

    @Deprecated(since = "1.5.6", forRemoval = true)
    private String template;

    @XComment("{CMS.AD_SPACE.TEMPLATES}")
    private List<PublishPipeTemplate> templates;

    @XComment("{CMS.AD_SPACE.PATH}")
    private String path;

    @XComment("{CC.ENTITY.REMARK}")
    private String remark;

    public Map<String, String> getPublishPipeTemplateMap() {
        if (StringUtils.isEmpty(this.templates)) {
            return Map.of();
        }
        return this.templates.stream().collect(Collectors.toMap(PublishPipeTemplate::code, PublishPipeTemplate::template));
    }
}
