package com.chestnut.word.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.word.domain.SensitiveWord;

/**
 * <p>
 * 敏感词Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

	@Select("<script>"
		+ "  select word from " + SensitiveWord.TABLE_NAME 
		+ "    <if test=\"type !=null and type != ''\">"
		+ "      where type = #{type}"
		+ "    </if>"
		+ "  </script>")
	public List<String> getWords(@Param("type") String type);
}
