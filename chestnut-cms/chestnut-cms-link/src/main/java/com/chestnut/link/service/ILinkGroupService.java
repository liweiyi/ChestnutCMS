package com.chestnut.link.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.link.domain.CmsLinkGroup;

public interface ILinkGroupService extends IService<CmsLinkGroup> {

	boolean deleteLinkGroup(List<Long> linkGroupIds);
}
