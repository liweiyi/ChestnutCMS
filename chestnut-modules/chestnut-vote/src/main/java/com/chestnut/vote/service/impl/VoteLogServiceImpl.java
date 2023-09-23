package com.chestnut.vote.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.vote.domain.VoteLog;
import com.chestnut.vote.mapper.VoteLogMapper;
import com.chestnut.vote.service.IVoteLogService;

@Service
public class VoteLogServiceImpl extends ServiceImpl<VoteLogMapper, VoteLog> implements IVoteLogService {

}