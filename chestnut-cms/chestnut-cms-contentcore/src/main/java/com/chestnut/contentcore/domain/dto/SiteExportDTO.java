package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SiteExportDTO extends BaseDTO {
	
	/**
	 * 所属站点ID
	 */
    @LongId
	private Long siteId;

    /**
     * 导出目录
     */
    private List<String> directories;
}
