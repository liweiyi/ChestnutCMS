package com.chestnut.member.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.member.domain.MemberLevelExpLog;
import com.chestnut.member.mapper.MemberLevelExpLogMapper;
import com.chestnut.member.service.IMemberLevelExpLogService;

@Service
public class MemberLevelExpLogServiceImpl extends ServiceImpl<MemberLevelExpLogMapper, MemberLevelExpLog>
		implements IMemberLevelExpLogService {
	
}