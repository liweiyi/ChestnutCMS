/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.contentcore.util;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.perms.BitSetPrivItem;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.perms.PageWidgetPermissionType;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.enums.PermissionOwnerType;

import java.util.*;
import java.util.stream.Stream;

/**
 * 内容核心权限工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CmsPrivUtils {

    /**
     * 注解`@Priv`权限验证判断站点权限用
     */
    public static final String PRIV_SITE_VIEW_PLACEHOLDER = SitePermissionType.ID + ":View:${#_header['CurrentSite']}";

    public static final String PRIV_SITE_EDIT_PLACEHOLDER = SitePermissionType.ID + ":Edit:${#_header['CurrentSite']}";


    /**
     * BitSet权限序列化
     *
     * @param permissionMap
     * @return
     */
    public static String serializeBitSetPermission(Map<String, BitSet> permissionMap) {
        Map<String, long[]> permsMap = new HashMap<>();
        permissionMap.forEach((key, value) -> permsMap.put(key, value.toLongArray()));
        return JacksonUtils.to(permsMap);
    }

    /**
     * BitSet权限反序列化
     *
     * @param json
     * @return
     */
    public static Map<String, BitSet> deserializeBitSetPermission(String json) {
        Map<String, BitSet> permissionMap = new HashMap<>();
        Map<String, long[]> privs = JacksonUtils.fromMap(json, long[].class);
        if (privs != null) {
            privs.forEach((key, value) -> permissionMap.put(key, BitSet.valueOf(value)));
        }
        return permissionMap;
    }

    /**
     * 合并多个权限配置
     *
     * @param permissionJsonList
     */
    public static String mergeBitSetPermissions(List<String> permissionJsonList) {
        Map<String, BitSet> map = new HashMap<>();
        permissionJsonList.forEach(json -> {
            Map<String, BitSet> bitSet = deserializeBitSetPermission(json);
            bitSet.entrySet().forEach(e -> {
                BitSet bs = map.get(e.getKey());
                if (bs == null) {
                    map.put(e.getKey(), e.getValue());
                } else {
                    bs.or(e.getValue());
                }
            });
        });
        return serializeBitSetPermission(map);
    }

    public static String getAllSitePermissions(Long siteId) {
        SitePermissionType.SitePrivItem[] privItems = SitePermissionType.SitePrivItem.values();
        BitSet bitSet = new BitSet(privItems.length);
        for (BitSetPrivItem item : privItems) {
            bitSet.set(item.bitIndex());
        }
        return serializeBitSetPermission(Map.of(siteId.toString(), bitSet));
    }

    public static String getAllCatalogPermissions(Long catalogId) {
        CatalogPermissionType.CatalogPrivItem[] privItems = CatalogPermissionType.CatalogPrivItem.values();
        BitSet bitSet = new BitSet(privItems.length);
        for (BitSetPrivItem item : privItems) {
            bitSet.set(item.bitIndex());
        }
        return serializeBitSetPermission(Map.of(catalogId.toString(), bitSet));
    }

    public static String getAllPageWidgetPermissions(Long catalogId) {
        PageWidgetPermissionType.PageWidgetPrivItem[] privItems = PageWidgetPermissionType.PageWidgetPrivItem.values();
        BitSet bitSet = new BitSet(privItems.length);
        for (BitSetPrivItem item : privItems) {
            bitSet.set(item.bitIndex());
        }
        return serializeBitSetPermission(Map.of(catalogId.toString(), bitSet));
    }

    /**
     * 校验perms是否包含指定权限
     *
     * @param key
     * @param privItem
     * @param perms
     * @return
     */
    public static boolean hasBitSetPermission(String key, BitSetPrivItem privItem, Map<String, BitSet> perms) {
        BitSet bitSet = perms.get(key);
        return Objects.nonNull(bitSet) && bitSet.get(privItem.bitIndex());
    }

    /**
     * 校验用户是否拥有指定权限
     *
     * @param permissionJson 权限持久化json字符串
     * @param key
     * @param privItem
     * @param loginUser
     * @return
     */
    public static boolean hasBitSetPermission(String permissionJson, String key, BitSetPrivItem privItem,
                                              LoginUser loginUser) {
        if (loginUser.isSuperAdministrator()) {
            return true;
        }
        List<Long> list = JacksonUtils.getAsList(permissionJson, key, Long.class);
        if (list != null) {
            BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
            if (bitSet.get(privItem.bitIndex())) {
                return true;
            }
        }
        return false;
    }
}
