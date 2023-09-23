package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.domain.dto.SysPermissionDTO;
import jakarta.annotation.Nullable;

import java.util.Set;

/**
 * 通用权限 业务层
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysPermissionService extends IService<SysPermission> {

	/** 所有权限标识 */
	String ALL_PERMISSION = "*";

	/**
	 * 获取权限信息
	 * 
	 * @param ownerType
	 * @param owner
	 * @return
	 */
	SysPermission getPermission(String ownerType, String owner);

	/**
	 * 保存权限数据
	 *
	 * @param ownerType
	 * @param owner
	 * @param perms
	 * @param permissionType
	 * @param operator
	 */
    void savePermissions(String ownerType, String owner, Set<String> perms, String permissionType, String operator);

    /**
	 * 保存菜单权限信息
	 * 
	 * @param dto
	 */
	void saveMenuPermissions(SysPermissionDTO dto);

	/**
	 * 获取用户权限列表
	 * 
	 * @param userId
	 * @param permissionType 指定权限类型
	 * @return
	 */
	Set<String> getUserPermissions(Long userId, @Nullable String permissionType);

	/**
	 * 重置登录用户权限信息
	 *
	 * @param loginUser 登录用户信息
	 */
	void resetLoginUserPermissions(LoginUser loginUser);

	/**
	 * 获取继承权限
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @return
	 */
	Set<String> getInheritedPermissionKeys(String ownerType, String owner, String permissionType);

	/**
	 * 授权
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 * @return
	 */
	SysPermission grantPermission(String ownerType, String owner, String permissionType, String permissionJson);
}
