package com.chestnut.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SysPostSelectVO {

	private Long postId;

	private String postCode;

	private String postName;
}
