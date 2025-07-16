package com.trackery.trackerybackapiserver.domain.common.response.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.response.enums
 * fileName       : SuccessCode
 * author         : durururuk
 * date           : 25. 2. 13.
 * description    : 애플리케이션에서 성공적인 HTTP 응답 상태를 정의한 enum 클래스입니다.
 * 					각 성공 코드는 HTTP 상태 코드, 성공 코드 그리고 성공 메시지를 포함하고 있습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 13.		durururuk		최초 생성
 * 25. 2. 13.		durururuk		기본 응답 포맷 작성
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 */
@Getter
@AllArgsConstructor
public enum SuccessCode {
	/**
	 * 요청이 성공적으로 처리됨 (HttpStatus.OK, 200, "Ok")
	 */
	OK(HttpStatus.OK, 200, "Ok"),

	/**
	 * 요청이 성공적으로 처리되었으며, 새로운 리소스가 생성됨 (HttpStatus.CREATED, 201, "Created")
	 */
	CREATED(HttpStatus.CREATED, 201, "Created"),

	/**
	 * 요청이 승인되었지만, 아직 처리가 완료되지 않음 (HttpStatus.ACCEPTED, 202, "Accepted")
	 */
	ACCEPTED(HttpStatus.ACCEPTED, 202, "Accepted"),

	/**
	 * 요청이 성공적으로 처리되었지만, 반환할 컨텐츠가 없음 (HttpStatus.NO_CONTENT, 204, "No Content")
	 */
	NO_CONTENT(HttpStatus.NO_CONTENT, 204, "No Content");


	/**
	 * HTTP 상태 코드
	 */
	private final HttpStatus status;

	/**
	 * HTTP 상태 코드의 정수 값
	 */
	private final int code;

	/**
	 * 오류 메시지
	 */
	private final String message;
}
