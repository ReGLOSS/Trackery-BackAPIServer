package com.trackery.trackerybackapiserver.domain.common.response.exception;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;

import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.response.exception
 * fileName       : ApiException
 * author         : durururuk
 * date           : 25. 2. 13.
 * description    : 애플리케이션에서 발생하는 예외를 처리하는 커스텀 예외 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 13.		durururuk		최초 생성
 * 25. 2. 13.		durururuk		기본 응답 포맷 작성
 * 25. 2. 13.		durururuk		예외 발생 시 응답을 생성하는 핸들러 작성
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 * 25. 3. 14.		durururuk		리팩터링
 */
@Getter
public class ApiException extends RuntimeException {

	/**
	 * 예외와 연결된 ErrorCode 객체
	 */
	private final ErrorCode errorCode;

	/**
	 * 지정된 ErrorCode를 사용하여 새로운 예외를 생성합니다.
	 *
	 * @param errorCode 발생한 예외 코드를 나타내는 객체
	 */
	public ApiException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
