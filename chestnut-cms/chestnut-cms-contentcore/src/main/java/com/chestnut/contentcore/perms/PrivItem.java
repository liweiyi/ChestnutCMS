package com.chestnut.contentcore.perms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PrivItem {

    public PrivItem(boolean granted, boolean isInherited) {
        this.granted = granted;
        this.isInherited = isInherited;
    }

    /**
     * 是否已授权
     */
    private boolean granted;

    /**
     * 是否是继承权限
     */
    private boolean isInherited;
}