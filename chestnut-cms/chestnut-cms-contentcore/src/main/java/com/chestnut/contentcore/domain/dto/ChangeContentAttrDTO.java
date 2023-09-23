package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 变更内容属性DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ChangeContentAttrDTO extends BaseDTO {

    @NotEmpty
    private List<Long> contentIds;

    @NotEmpty
    private String attr;
}
