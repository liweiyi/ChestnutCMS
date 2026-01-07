/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.dto.CreateMenuRequest;
import com.chestnut.system.domain.dto.UpdateMenuRequest;
import com.chestnut.system.domain.vo.MetaVO;
import com.chestnut.system.domain.vo.RouterVO;
import com.chestnut.system.enums.MenuComponentType;
import com.chestnut.system.enums.MenuType;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.mapper.SysMenuMapper;
import com.chestnut.system.service.ISysI18nDictService;
import com.chestnut.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

	private final ISysI18nDictService i18nDictService;

	/**
	 * 构建前端路由所需要的菜单
	 *
	 * @param menus 菜单列表
	 * @return 路由列表
	 */
	@Override
	public List<RouterVO> buildRouters(List<SysMenu> menus) {
		List<RouterVO> routers = new LinkedList<>();
		for (SysMenu menu : menus) {
			RouterVO router = new RouterVO();
			router.setHidden(YesOrNo.isNo(menu.getVisible()));
			router.setName(getRouteName(menu));
			router.setPath(getRouterPath(menu));
			router.setComponent(getComponent(menu));
			router.setQuery(menu.getQuery());
			router.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(), YesOrNo.isNo(menu.getIsCache()),
					menu.getPath()));
			List<SysMenu> cMenus = menu.getChildren();
			if (!cMenus.isEmpty() && MenuType.isDirectory(menu.getMenuType())) {
				router.setAlwaysShow(true);
				router.setRedirect("noRedirect");
				router.setChildren(buildRouters(cMenus));
			} else if (isMenuFrame(menu)) {
				router.setMeta(null);
				List<RouterVO> childrenList = new ArrayList<>();
				RouterVO children = new RouterVO();
				children.setPath(menu.getPath());
				children.setComponent(menu.getComponent());
				children.setName(parseRouteName(menu));
				children.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(),
						YesOrNo.isNo(menu.getIsCache()), menu.getPath()));
				children.setQuery(menu.getQuery());
				childrenList.add(children);
				router.setChildren(childrenList);
			} else if (menu.getParentId().intValue() == 0 && isInnerLink(menu)) {
				router.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon()));
				router.setPath("/");
				List<RouterVO> childrenList = new ArrayList<>();
				RouterVO children = new RouterVO();
				String routerPath = innerLinkReplaceEach(menu.getPath());
				children.setPath(routerPath);
				children.setComponent(MenuComponentType.InnerLink.name());
				children.setName(parseRouteName(menu));
				children.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(), menu.getPath()));
				childrenList.add(children);
				router.setChildren(childrenList);
			}
			routers.add(router);
		}
		return routers;
	}

	@Override
	public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
		List<SysMenu> returnList = new ArrayList<SysMenu>();
		List<Long> tempList = new ArrayList<Long>();
		for (SysMenu dept : menus) {
			tempList.add(dept.getMenuId());
		}
		for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext();) {
			SysMenu menu = (SysMenu) iterator.next();
			// 如果是顶级节点, 遍历该父节点的所有子节点
			if (!tempList.contains(menu.getParentId())) {
				recursionFn(menus, menu);
				returnList.add(menu);
			}
		}
		if (returnList.isEmpty()) {
			returnList = menus;
		}
		return returnList;
	}

	@Override
	public List<TreeNode<Long>> buildMenuTreeSelect(List<SysMenu> menus) {
		List<SysMenu> menuTrees = buildMenuTree(menus);
		return menuTrees.stream().map(this::buildTreeSelect).collect(Collectors.toList());
	}

	private TreeNode<Long> buildTreeSelect(SysMenu menu) {
		TreeNode<Long> node = new TreeNode<Long>(menu.getMenuId(), menu.getParentId(), menu.getMenuName(), false);
		if (MenuType.Directory.value().equals(menu.getMenuType())) {
			node.setDefaultExpanded(true);
		}
        node.setProps(Map.of("type", menu.getMenuType()));
		List<TreeNode<Long>> children = menu.getChildren().stream().map(this::buildTreeSelect)
				.collect(Collectors.toList());
		node.setChildren(children);
		return node;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertMenu(CreateMenuRequest req) {
		boolean checkFrameUrl = YesOrNo.isYes(req.getIsFrame()) && !ServletUtils.isHttpUrl(req.getPath());
		Assert.isFalse(checkFrameUrl, () -> CommonErrorCode.SYSTEM_ERROR.exception("The path must start with http(s)://"));

		boolean checkMenuUnique = this.checkMenuUnique(req.getMenuName(), req.getParentId(), null);
		Assert.isTrue(checkMenuUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("menuName"));

		SysMenu menu = new SysMenu();
		BeanUtils.copyProperties(req, menu);
		menu.setMenuId(IdUtils.getSnowflakeId());
		menu.createBy(req.getOperator().getUsername());
		this.save(menu);

		i18nDictService.saveOrUpdate(LocaleContextHolder.getLocale().toLanguageTag(),
				langKey(menu.getMenuId()), menu.getMenuName());
	}

	private boolean checkMenuUnique(String menuName, Long parentId, Long menuId) {
		LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuName, menuName)
				.eq(SysMenu::getParentId, parentId).ne(IdUtils.validate(menuId), SysMenu::getMenuId, menuId);
		return this.count(q) == 0;
	}

	@Override
	public void updateMenu(UpdateMenuRequest req) {
		SysMenu db = this.getById(req.getMenuId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getMenuId()));
		Assert.isFalse(req.getParentId().equals(req.getMenuId()),
				() -> CommonErrorCode.INVALID_REQUEST_ARG.exception("The parent cannot be it self."));
		boolean checkFrameUrl = YesOrNo.isYes(req.getIsFrame()) && !ServletUtils.isHttpUrl(req.getPath());
		Assert.isFalse(checkFrameUrl, () -> CommonErrorCode.SYSTEM_ERROR.exception("The path must start with http(s)://"));
		boolean checkMenuUnique = this.checkMenuUnique(req.getMenuName(), req.getParentId(), req.getMenuId());
		Assert.isTrue(checkMenuUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("menuName"));

		BeanUtils.copyProperties(req, db);
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
	}

	@Override
	public void deleteMenuById(Long menuId) {
		boolean hasChild = this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId)) > 0;
		Assert.isFalse(hasChild, SysErrorCode.MENU_DEL_CHILD_FIRST::exception);
		this.removeById(menuId);
		// 删除国际化配置
		this.i18nDictService.deleteByLangKey(langKey(menuId), false);
	}

	private String langKey(Long menuId) {
		return "MENU.NAME." + menuId;
	}

	public String getRouteName(SysMenu menu) {
		if (StringUtils.isEmpty(menu.getComponent()) || isMenuFrame(menu)) {
			return StringUtils.EMPTY;
		}
		return parseRouteName(menu);
	}

	private String parseRouteName(SysMenu menu) {
		if (StringUtils.isEmpty(menu.getComponent())) {
			return StringUtils.EMPTY;
		}
		return Arrays.stream(menu.getComponent().split("/"))
				.map(StringUtils::capitalize).collect(Collectors.joining(""));
	}

	public String getRouterPath(SysMenu menu) {
		String routerPath = menu.getPath();
		// 内链打开外网方式
		if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
			routerPath = innerLinkReplaceEach(routerPath);
		}
		// 非外链并且是一级目录（类型为目录）
		if (0 == menu.getParentId().intValue() && MenuType.isDirectory(menu.getMenuType())
				&& YesOrNo.isNo(menu.getIsFrame())) {
			routerPath = "/" + menu.getPath();
		}
		// 非外链并且是一级目录（类型为菜单）
		else if (isMenuFrame(menu)) {
			routerPath = "/";
		}
		return routerPath;
	}

	public String getComponent(SysMenu menu) {
		String component = MenuComponentType.Layout.name();
		if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
			component = menu.getComponent();
		} else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0
				&& isInnerLink(menu)) {
			component = MenuComponentType.InnerLink.name();
		} else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
			component = MenuComponentType.ParentView.name();
		}
		return component;
	}

	public boolean isMenuFrame(SysMenu menu) {
		return menu.getParentId().intValue() == 0 && MenuType.isMenu(menu.getMenuType())
				&& YesOrNo.isNo(menu.getIsFrame());
	}

	public boolean isInnerLink(SysMenu menu) {
		return YesOrNo.isNo(menu.getIsFrame()) && ServletUtils.isHttpUrl(menu.getPath());
	}

	public boolean isParentView(SysMenu menu) {
		return menu.getParentId().intValue() != 0 && MenuType.isDirectory(menu.getMenuType());
	}

	@Override
	public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
		List<SysMenu> returnList = new ArrayList<>();
		for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext();) {
			SysMenu t = iterator.next();
			// 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
			if (t.getParentId() == parentId) {
				recursionFn(list, t);
				returnList.add(t);
			}
		}
		return returnList;
	}

	/**
	 * 递归列表
	 */
	private void recursionFn(List<SysMenu> list, SysMenu t) {
		// 得到子节点列表
		List<SysMenu> childList = getChildList(list, t);
		t.setChildren(childList);
		for (SysMenu tChild : childList) {
			if (hasChild(list, tChild)) {
				recursionFn(list, tChild);
			}
		}
	}

	/**
	 * 得到子节点列表
	 */
	private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
		List<SysMenu> tlist = new ArrayList<>();
        for (SysMenu n : list) {
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
		return tlist;
	}

	/**
	 * 判断是否有子节点
	 */
	private boolean hasChild(List<SysMenu> list, SysMenu t) {
		return !getChildList(list, t).isEmpty();
	}

	/**
	 * 内链域名特殊字符替换
	 */
	public String innerLinkReplaceEach(String path) {
		return StringUtils.replaceEach(path,
				new String[] { ServletUtils.HTTP, ServletUtils.HTTPS, ServletUtils.WWW, StringUtils.DOT },
				new String[] { StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.SLASH });
	}
}
