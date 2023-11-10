package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.contentcore.domain.CmsContentRela;
import com.chestnut.contentcore.mapper.CmsContentRelaMapper;
import com.chestnut.contentcore.service.IContentRelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentRelaServiceImpl extends ServiceImpl<CmsContentRelaMapper, CmsContentRela>
        implements IContentRelaService {
}
