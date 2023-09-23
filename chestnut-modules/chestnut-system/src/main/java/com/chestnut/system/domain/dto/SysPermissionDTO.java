package com.chestnut.system.domain.dto;

import java.util.Set;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SysPermissionDTO extends BaseDTO {

	@NotEmpty
	private String ownerType;

	@NotEmpty
	private String owner;

	@NotEmpty
	private String permType;

	private Set<String> permissions;
}
