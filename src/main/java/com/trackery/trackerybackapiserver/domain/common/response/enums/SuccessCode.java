package com.trackery.trackerybackapiserver.domain.common.response.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
	OK(HttpStatus.OK, 200, "Ok"),
	CREATED(HttpStatus.CREATED, 201, "Created"),
	ACCEPTED(HttpStatus.ACCEPTED, 202, "Accepted"),
	NO_CONTENT(HttpStatus.NO_CONTENT, 204, "No Content");


	private final HttpStatus status;
	private final int code;
	private final String message;
}
