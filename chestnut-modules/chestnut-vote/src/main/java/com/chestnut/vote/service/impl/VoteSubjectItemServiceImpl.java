package com.chestnut.vote.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.vote.domain.VoteSubjectItem;
import com.chestnut.vote.mapper.VoteSubjectItemMapper;
import com.chestnut.vote.service.IVoteSubjectItemService;

@Service
public class VoteSubjectItemServiceImpl extends ServiceImpl<VoteSubjectItemMapper, VoteSubjectItem>
		implements IVoteSubjectItemService {

}