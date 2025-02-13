package com.trackery.trackerybackapiserver.domain.common.response;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

@RestControllerAdvice
public class ApiExceptionHandler {

	/**
	 * 서비스 로직 중 던져진 ApiException 처리하는 핸들러
	 * @param ex : 개발자가 던진 ApiException
	 * @return : 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
		return ResponseEntity
			.status(ex.getErrorCode().getCode())
			.body(ApiResponse.error(ex.getErrorCode()));
	}

	/**
	 * Validation 실패 시 발생하는 MethodArgumentNotValidException 처리하는 핸들러
	 * @param ex : Validation 실패 시 발생하는 예외 MethodArgumentNotValidException
	 * @return : 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(value = {
		MethodArgumentNotValidException.class
	})
	public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
		return ResponseEntity
			.status(400)
			.body(ApiResponse.error(ErrorCode.BAD_REQUEST,
				ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
	}
}
