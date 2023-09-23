package com.chestnut.contentcore.domain.dto;

import com.chestnut.contentcore.domain.vo.CatalogPrivVO;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveCatalogPermissionDTO {

    @NotEmpty
    private String ownerType;

    @NotEmpty
    private String owner;

    @LongId
    private Long siteId;

    private List<CatalogPrivVO> perms;
}