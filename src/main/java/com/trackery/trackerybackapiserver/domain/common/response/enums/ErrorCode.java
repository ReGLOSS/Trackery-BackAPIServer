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
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, 502, "Bad Gateway"),

	BAD_REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "Validation 실패"),
	BAD_REQUEST_INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, 400, "잘못된 Request Body"),

	INTERNAL_SERVER_ERROR_NO_SUCH_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, 500, "SHA-256 알고리즘을 사용할 수 없습니다."),
	INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "유틸리티 클래스 객체가 생성되었습니다.");

	private final HttpStatus status;
	private final int code;
	private final String message;
}
