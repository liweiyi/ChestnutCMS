package com.chestnut.cloud.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateCloudConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateCloudConfigRequest extends BaseDTO {

    @NotBlank
    @Length(max = 50)
    private String type;

    @NotBlank
    @Length(max = 100)
    private String configName;

    @Length(max = 100)
    private String configDesc;

    @NotBlank
    @Dict(EnableOrDisable.TYPE)
    private String status;

    @NotNull
    private ObjectNode configProps;

    @Length(max = 500)
    private String remark;
}
