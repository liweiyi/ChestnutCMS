package com.chestnut.member.listener.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会员收藏数据保存前事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class BeforeMemberFavoriteEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String dataType;

    private Long dataId;

    public BeforeMemberFavoriteEvent(Object source, String dataType, Long dataId) {
        super(source);
        this.dataType = dataType;
        this.dataId = dataId;
    }
}
