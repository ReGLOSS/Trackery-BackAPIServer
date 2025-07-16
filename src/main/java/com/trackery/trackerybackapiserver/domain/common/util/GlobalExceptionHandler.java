package com.trackery.trackerybackapiserver.domain.common.util;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : GlobalExceptionHandler
 * author         : durururuk
 * date           : 25. 2. 13.
 * description    : 글로벌 예외를 처리하는 클래스입니다.
 * 					컨트롤러에서 발생하는 예외를 처리하여 일관된 응답 형식을 제공하며
 * 					로깅을 통해 예외 정보를 기록합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 13.		durururuk		최초 생성
 * 25. 2. 13.		durururuk		예외 발생 시 응답을 생성하는 핸들러 작성
 * 25. 2. 13.		durururuk		Validation 관련 예외 발생 시 처리할 수 있게 ApiExceptionHandler 수정
 * 25. 2. 13.		durururuk		사용하지 않는 import 제거
 * 25. 2. 13.		durururuk		핸들러 주석 추가, 줄바꿈 서식 변경
 * 25. 2. 14.		durururuk		Validation 실패 예외, 잘못된 RequestBody 예외 처리하는 ErrorCode, 핸들러 수정
 * 25. 2. 14.		durururuk		회원가입 할 때 비밀번호를 해싱해서 저장하게 수정
 * 25. 2. 14.		durururuk		서식 수정
 * 25. 2. 15.		durururuk		ApiExceptionHandler 사용처에 맞게 이름 변경
 * 25. 2. 17.		Nari-Lee		공통응답 테스트코드 추가
 * 25. 2. 21.        inari			 상세 주석 추가
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 * 25. 3. 26.		Nari-Lee		provider enum 적용
 * 25. 3. 26.        inari			 handleMethodArgumentTypeMismatchException 추가
 * 25. 3. 26.        inari			 handleRuntimeException 추가
 * 25. 4. 8.		Nari-Lee		GlobalExceptionHandle로 예외 위임
 */
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

	/**
	 * 처리되지 않은 예외를 처리하는 핸들러
	 *
	 * @param ex 처리되지 않은 예외
	 * @return 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
		log.error("처리되지 않은 예외 발생: {}", ex.getMessage(), ex);

		return ResponseEntity
			.status(500)
			.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	/**
	 * Enum 타입의 파라미터에 대한 요청값이 유효하지 않은 경우를 처리하는 핸들러
	 *
	 * @param ex Enum 타입의 파라미터에 대한 요청값이 유효하지 않은 경우 발생하는 예외 MethodArgumentTypeMismatchException
	 * @return 실패 코드, 메시지를 담은 응답 포맷 반환
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex) {

		// 요청된 enum 값이 유효하지 않은 경우 처리
		if (ex.getParameter().getParameterType().isEnum()) {
			log.error("잘못된 Enum 값: {}", ex.getValue());
			return ResponseEntity.badRequest()
				.body(ApiResponse.error(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER));
		}

		return ResponseEntity.badRequest()
			.body(ApiResponse.error(ErrorCode.BAD_REQUEST));
	}
}
