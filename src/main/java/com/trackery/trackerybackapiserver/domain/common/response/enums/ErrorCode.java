package com.trackery.trackerybackapiserver.domain.common.response.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "Unauthorized"),
	FORBIDDEN(HttpStatus.FORBIDDEN, 403, "Forbidden"),
	NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Not Found"),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),
	NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, 501, "Not Implemented"),
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, 502, "Bad Gateway");

	private final HttpStatus status;
	private final int code;
	private final String message;
}
