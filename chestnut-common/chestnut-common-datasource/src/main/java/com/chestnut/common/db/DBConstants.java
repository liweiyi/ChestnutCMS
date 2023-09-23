package com.chestnut.common.db;

/**
 * DB 公用变量
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class DBConstants {

    /**
     * 逻辑删除标识：未删除
     */
    public static final int DELETED_NO = 0;

    /**
     * 逻辑删除标识：已删除
     */
    public static final int DELETED_YES = 1;

    public static boolean isDeleted(Integer deleted) {
        return deleted != null && deleted.intValue() == DELETED_YES;
    }
}
