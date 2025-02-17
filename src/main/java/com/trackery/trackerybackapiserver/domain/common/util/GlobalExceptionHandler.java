package com.trackery.trackerybackapiserver.domain.common.util;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 서비스 로직 중 던져진 예외를 처리하는 핸들러
	 *
	 * @param ex : 개발자가 던진 ApiException
	 * @return : 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
			.status(ex.getErrorCode().getCode())
			.body(ApiResponse.error(ex.getErrorCode()));
	}

	/**
	 * Validation 실패 시 발생하는 예외를 처리하는 핸들러
	 *
	 * @param ex : Validation 실패 시 발생하는 예외 MethodArgumentNotValidException
	 * @return : 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(value = {
		MethodArgumentNotValidException.class
	})
	public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
			.status(400)
			.body(ApiResponse.error(ErrorCode.BAD_REQUEST_VALIDATION_FAILED,
				ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
	}

	/**
	 * Request Body 잘못되었을 때 발생하는 예외를 처리하는 핸들러
	 *
	 * @param ex Request Body 잘못되었을 때 발생하는 HttpMessageNotReadableException
	 * @return 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
			.status(400)
			.body(ApiResponse.error(ErrorCode.BAD_REQUEST_INVALID_REQUEST_BODY));
	}
}
