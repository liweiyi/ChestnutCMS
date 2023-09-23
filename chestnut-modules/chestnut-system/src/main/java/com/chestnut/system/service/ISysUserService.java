package com.chestnut.system.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysUser;

/**
 * 用户 业务层
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * 根据用户ID查询用户所属角色组
	 * 
	 * @param userName
	 *            用户名
	 * @return 结果
	 */
	public String selectUserRoleGroup(Long userId);

	/**
	 * 根据用户ID查询用户所属岗位组
	 * 
	 * @param userName
	 *            用户名
	 * @return 结果
	 */
	public String selectUserPostGroup(Long userId);

	/**
	 * 校验用户名称是否唯一
	 * 
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public boolean checkUserNameUnique(String username, Long userId);

	/**
	 * 校验手机号码是否唯一
	 *
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public boolean checkPhoneUnique(String phoneNumber, Long userId);

	/**
	 * 校验email是否唯一
	 *
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public boolean checkEmailUnique(String email, Long userId);

	/**
	 * 新增用户信息
	 * 
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public void insertUser(SysUser user);

	/**
	 * 注册用户信息
	 * 
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public void registerUser(SysUser user);

	/**
	 * 修改用户信息
	 * 
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public void updateUser(SysUser user);

	/**
	 * 用户授权角色
	 * 
	 * @param userId
	 *            用户ID
	 * @param roleIds
	 *            角色组
	 */
	public void insertUserAuth(Long userId, List<Long> roleIds);

	/**
	 * 重置用户密码
	 * 
	 * @param user
	 *            用户信息
	 * @return 结果
	 */
	public void resetPwd(SysUser user);

	/**
	 * 批量删除用户信息
	 * 
	 * @param userIds
	 *            需要删除的用户ID
	 * @return 结果
	 */
	public void deleteUserByIds(List<Long> userIds);

	/**
	 * 解锁用户
	 */
	void unlockUser(Long userId);

	/**
	 * 上传用户头像
	 * 
	 * @param userId
	 * @param file
	 * @return 头像文件相对路径
	 */
	public String uploadAvatar(Long userId, MultipartFile file);
}
