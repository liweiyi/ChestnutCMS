package com.chestnut.common.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDTO {

	@JsonIgnore
	private LoginUser operator;
}
