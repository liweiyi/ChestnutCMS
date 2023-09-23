package com.chestnut.advertisement.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.advertisement.domain.CmsAdHourStat;
import com.chestnut.advertisement.mapper.CmsAdHourStatMapper;
import com.chestnut.advertisement.service.IAdHourStatService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 广告统计 服务实现类
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class AdHourStatServiceImpl extends ServiceImpl<CmsAdHourStatMapper, CmsAdHourStat>
		implements IAdHourStatService {

}
