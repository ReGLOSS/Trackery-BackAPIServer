package com.trackery.trackerybackapiserver.domain.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "message", "data"})
public class ApiResponse<T> {

	private final int code;
	private final String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
		return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
	}

	public static <T> ApiResponse<T> success(SuccessCode successCode) {
		return success(successCode, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), data);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return error(errorCode, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
		return new ApiResponse<>(errorCode.getCode(), message, data);
	}
}
