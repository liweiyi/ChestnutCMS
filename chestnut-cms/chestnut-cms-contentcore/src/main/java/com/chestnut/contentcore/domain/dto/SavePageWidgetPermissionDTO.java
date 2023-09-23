package com.chestnut.contentcore.domain.dto;

import com.chestnut.contentcore.domain.vo.PageWidgetPrivVO;
import com.chestnut.contentcore.domain.vo.SitePrivVO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SavePageWidgetPermissionDTO {

    @NotEmpty
    private String ownerType;

    @NotEmpty
    private String owner;

    private List<PageWidgetPrivVO> perms;
}