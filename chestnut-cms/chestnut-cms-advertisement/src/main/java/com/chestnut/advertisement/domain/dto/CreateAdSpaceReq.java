package com.chestnut.advertisement.domain.dto;

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
@Getter
@Setter
public class CreateAdSpaceReq extends BaseDTO {

    /**
     * 名称
     */
    @NotEmpty
    private String name;

    /**
     * 编码
     */
    @NotEmpty
    private String code;

    /**
     * 发布通道编码
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String publishPipeCode;

    /**
     * 模板路径
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String template;

    /**
     * 发布通道模板配置
     */
    private List<PublishPipeTemplate> templates;

    /**
     * 静态化目录
     */
    private String path;

    /**
     * 备注
     */
    private String remark;

    public Map<String, String> getPublishPipeTemplateMap() {
        if (StringUtils.isEmpty(this.templates)) {
            return Map.of();
        }
        return this.templates.stream().collect(Collectors.toMap(PublishPipeTemplate::code, PublishPipeTemplate::template));
    }
}
