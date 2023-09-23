package com.chestnut.common.db.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 逻辑删除entity
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class BaseEntityWithLogicDelete extends BaseEntity {

    /**
     * 逻辑删除标识
     */
    private Integer deleted;
}
