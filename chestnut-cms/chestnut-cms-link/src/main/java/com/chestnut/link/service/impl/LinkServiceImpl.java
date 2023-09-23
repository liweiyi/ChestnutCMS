package com.chestnut.link.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.mapper.CmsLinkMapper;
import com.chestnut.link.service.ILinkService;

@Service
public class LinkServiceImpl extends ServiceImpl<CmsLinkMapper, CmsLink> implements ILinkService {

}
