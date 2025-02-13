package com.trackery.trackerybackapiserver.domain.common.response;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
		return ResponseEntity
			.status(ex.getErrorCode().getCode())
			.body(ApiResponse.error(ex.getErrorCode()));
	}

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
