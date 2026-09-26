package com.chestnut.system.user;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.validation.BeanValidators;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.CreateUserRequest;
import com.chestnut.system.domain.dto.UpdateUserRequest;
import com.chestnut.system.domain.dto.UserImportData;
import com.chestnut.system.exception.SystemUserTips;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysPostService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.service.impl.UserPermissionService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
public class SysUserReadListener implements ReadListener<UserImportData> {

	private static final Logger log = LoggerFactory.getLogger(SysUserReadListener.class);

	private final ISysUserService userService;

	private final ISysDeptService deptService;

	private final ISysPostService postService;

	private final UserPermissionService userPermissionService;

	@Setter
	private Validator validator;

	@Setter
	private boolean isUpdateSupport;

	@Setter
	private LoginUser operator;

	private int successCount;

	private int failCount;

	@Setter
	private StringWriter logWriter;

	@Setter
	private Locale locale;

	@Override
	public void invoke(UserImportData data, AnalysisContext context) {
		try {
			log.debug(context.readRowHolder().getRowIndex() + "/"
					+ context.readSheetHolder().getApproximateTotalRowNumber() + ": " + data.getUserName());
			SysUser u = this.userService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, data.getUserName()));
			if (Objects.isNull(u)) {
				List<SysDept> deptList = this.deptService.getDepartmentsByUserId(operator.getUserId(),
						q -> q.eq(SysDept::getDeptName, data.getDeptName()));
				if (deptList.size() != 1) {
					failCount++;
					logWriter.append(SystemUserTips.DEPT_NOT_EXISTS_OR_DUPLICATE.locale(locale, data.getDeptName())).append("<br/>");
					return;
				}
				SysDept dept = deptList.get(0);

				BeanValidators.validateWithException(this.validator, data);
				u = new SysUser();
				u.setUserId(IdUtils.getSnowflakeId());
				u.setDeptId(dept.getDeptId());
				if (StringUtils.isNotEmpty(data.getPostCodes())) {
					Long[] postIds = data.getPostCodes().stream().map(postCode -> {
						SysPost post = this.postService.getPost(postCode);
						return Objects.isNull(post) ? 0L : post.getPostId();
					}).filter(IdUtils::validate).toArray(Long[]::new);
					u.setPostIds(postIds);
				}
				u.setUserName(data.getUserName());
				u.setNickName(data.getNickName());
				u.setPassword(data.getPassword());
				u.setPhoneNumber(data.getPhoneNumber());
				u.setEmail(data.getEmail());
				u.setSex(data.getGender());
				u.setStatus(data.getStatus());
				u.setRemark(data.getRemark());
				u.setPassword(data.getPassword());
				u.setCreateBy(this.operator.getUsername());
				CreateUserRequest req = new CreateUserRequest();
				BeanUtils.copyProperties(u, req);
				req.setOperator(operator);
				this.userService.insertUser(req);
				successCount++;
			} else if (this.isUpdateSupport) {
				BeanValidators.validateWithException(this.validator, data);
				u.setUserName(data.getUserName());
				u.setNickName(data.getNickName());
				u.setPhoneNumber(data.getPhoneNumber());
				u.setEmail(data.getEmail());
				u.setSex(data.getGender());
				u.setStatus(data.getStatus());
				u.setRemark(data.getRemark());
				u.setPassword(data.getPassword());
				u.setUpdateBy(this.operator.getUsername());
				UpdateUserRequest req = new UpdateUserRequest();
				BeanUtils.copyProperties(u, req);
				req.setOperator(operator);
				this.userService.updateUser(req);
				successCount++;
			} else {
				failCount++;
				logWriter.append(SystemUserTips.USER_EXISTS.locale(locale, data.getUserName())).append("<br/>");
			}
		} catch (Exception e) {
			failCount++;
			logWriter.append(SystemUserTips.IMPORT_FAIL.locale(locale, data.getUserName(), e.getMessage())).append("<br/>");
		}
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		logWriter.append(SystemUserTips.IMPORT_FAIL.locale(locale, successCount, failCount));
	}
}