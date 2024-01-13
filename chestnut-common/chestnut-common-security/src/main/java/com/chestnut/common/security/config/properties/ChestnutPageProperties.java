package com.chestnut.common.security.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 列表分页参数配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.page")
public class ChestnutPageProperties {

    /**
     * 默认分页起始页码
     */
    private int startPageNumber = 1;

    /**
     * 默认分页数
     */
    private int defaultPageSize = 20;

    /**
     * 分页数上限
     */
    private int maxPageSize = 10000;
}
