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

	/**
	 * 데이터가 포함 된 성공 시 응답 포맷
	 * @param successCode : 요청 처리 성공 시 응답 코드, 메시지
	 * @param data : 반환 될 데이터
	 * @return : 응답 코드, 메시지, 데이터를 담은 응답 포맷
	 */
	public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
		return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
	}

	/**
	 * 데이터가 포함되지 않은 성공 시 응답 포맷
	 * @param successCode 요청 처리 성공 시 응답 코드, 메시지
	 * @return 응답 코드, 메시지를 담은 응답 포맷
	 */
	public static <T> ApiResponse<T> success(SuccessCode successCode) {
		return success(successCode, null);
	}

	/**
	 * 데이터가 포함 된 실패 시 응답 포맷
	 * @param errorCode 요청 처리 실패 시 응답 코드, 메시지
	 * @param data : 실패 시 반환해야 할 데이터
	 * @return 응답 코드, 메시지, 데이터를 담은 응답 포맷
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), data);
	}

	/**
	 * 데이터가 포함되지 않은 실패 시 응답 포맷
	 * @param errorCode 요청 처리 실패 시 응답 코드, 메시지
	 * @return 응답 코드, 메시지를 담은 응답 포맷
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return error(errorCode, null);
	}

	/**
	 * 데이터를 포함하고 추가적인 메시지를 담은 실패 시 응답 포맷
	 * @param errorCode 요청 처리 실패 시 기본 응답 코드
	 * @param message 따로 작성된 메시지
	 * @param data 실패 시 반환돼야 할 데이터
	 * @return 응답 코드, 메시지, 데이터를 담은 응답 포맷
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
		return new ApiResponse<>(errorCode.getCode(), message, data);
	}
}
