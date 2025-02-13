package com.trackery.trackerybackapiserver.domain.common.response.exception;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
	private final ErrorCode errorCode;

	public ApiException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
