package com.chestnut.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysUserPost;

/**
 * 岗位信息 数据层
 */
public interface SysPostMapper extends BaseMapper<SysPost> {

	/**
	 * 删除指定用户岗位关联记录
	 * 
	 * @param userIds
	 * @return
	 */
	@Delete({ "<script>DELETE FROM " + SysUserPost.TABLE_NAME, 
		" WHERE user_id in ",
		"   <foreach collection=\"userIds\" item=\"userId\" index=\"index\" separator=\",\" open=\"(\" close=\")\">",
		"   #{userId}",
		"   </foreach>",
		" </script>"})
	public int deleteUserPost(@Param("userIds") Long... userIds);

	/**
	 * 查询指定用户关联岗位列表
	 * 
	 * @param userId
	 * @return
	 */
	@Select("SELECT a.* FROM " + SysUserPost.TABLE_NAME + " a LEFT JOIN " + SysPost.TABLE_NAME
			+ " b ON a.post_id = b.post_id WHERE a.user_id = #{userId}")
	public List<SysPost> selectPostsByUserId(@Param("userId") Long userId);

	/**
	 * 查询指定用户所有岗位ID
	 * 
	 * @param userId
	 * @return
	 */
	@Select("SELECT post_id FROM " + SysUserPost.TABLE_NAME + " WHERE user_id = #{userId}")
	public List<Long> selectUserPostIds(@Param("userId") Long userId);

	/**
	 * 查询指定岗位用户数
	 * 
	 * @param postId
	 * @return
	 */
	@Select("SELECT count(1) FROM " + SysUserPost.TABLE_NAME + " WHERE post_id = #{postId}")
	public Long getUserCountByPostId(@Param("postId") Long postId);

	/**
	 * 插入用户与岗位关联数据
	 * 
	 * @param userId
	 * @param postId
	 */
	@Insert("INSERT INTO " + SysUserPost.TABLE_NAME + " VALUES (#{userId}, #{postId})")
	public void insertUserPost(@Param("userId") Long userId, @Param("postId") Long postId);
}
