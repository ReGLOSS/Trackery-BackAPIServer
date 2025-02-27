package com.trackery.trackerybackapiserver.domain.common.response.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.response.enums
 * fileName       : ErrorCode
 * author         : durururuk
 * date           : 25. 2. 13.
 * description    : 애플리케이션에서 발생할 수 있는 다양한 오류 상태를 정의한 enum 클래스입니다.
 * 					각 오류 코드는 HTTP 상태 코드, 오류 코드 그리고 오류 메시지를 포함하고 있습니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 13.        durururuk       최초 생성
 * 25. 2. 21.        inari			 상세 주석 추가
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

	/**
	 * 잘못된 요청 (HttpStatus.BAD_REQUEST, 400, "Bad Request")
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),

	/**
	 * 인증 되지 않은 요청 (HttpStatus.UNAUTHORIZED, 401, "Unauthorized")(
	 */
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "Unauthorized"),

	/**
	 * 접근이 금지된 요청 (HttpStatus.FORBIDDEN, 403, "Forbidden")
	 */
	FORBIDDEN(HttpStatus.FORBIDDEN, 403, "Forbidden"),

	/**
	 * 요청한 리소스를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "Not Found")
	 */
	NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Not Found"),

	/**
	 * 내부 서버 오류 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error")
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),

	/**
	 * 구현되지 않은 기능 요청 (HttpStatus.NOT_IMPLEMENTED, 501, "Not Implemented")
	 */
	NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, 501, "Not Implemented"),

	/**
	 * 잘못된 게이트웨이 (HttpStatus.BAD_GATEWAY, 502, "Bad Gateway")
	 */
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, 502, "Bad Gateway"),

	/**
	 * 요청 데이터 유효성 검사 실패 (HttpStatus.BAD_REQUEST, 400, "Validation 실패")
	 */
	BAD_REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "Validation 실패"),

	/**
	 * 잘못된 요청 본문 (HttpStatus.BAD_REQUEST, 400, "잘못된 Request Body")
	 */
	BAD_REQUEST_INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, 400, "잘못된 Request Body"),


	/**
	 * SHA-256 알고리즘을 사용할 수 없음 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "SHA-256 알고리즘을 사용할 수 없습니다.")
	 */
	INTERNAL_SERVER_ERROR_NO_SUCH_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, 500, "SHA-256 알고리즘을 사용할 수 없습니다."),

	/**
	 * 유틸리티 클래스의 객체가 생성됨 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "유틸리티 클래스 객체가 생성되었습니다.")
	 */
	INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "유틸리티 클래스 객체가 생성되었습니다."),

	/**
	 * 데이터베이스에서 이미지 URL을 조회하는 중 오류 발생 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 URL 조회중 오류가 발생했습니다.")
	 */
	INTERNAL_SERVER_ERROR_LOAD_DATABASE(HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 URL 조회중 오류가 발생했습니다."),

	/**
	 * 요청한 이미지를 불러올 수 없음 (HttpStatus.NOT_FOUND, 404, "이미지를 불러올 수 없습니다.")
	 */
	NOT_FOUND_IMAGE(HttpStatus.NOT_FOUND, 404, "이미지를 불러올 수 없습니다."),

	/**
	 * JWT 토큰 생성 실패 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "JWT 토큰 생성에 실패했습니다.")
	 */
	INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT(HttpStatus.INTERNAL_SERVER_ERROR, 500, "JWT 토큰 생성에 실패했습니다."),

	/**
	 * JWT 검증 실패 (HttpStatus.UNAUTHORIZED, 401, "JWT 검증에 실패했습니다.")
	 */
	UNAUTHORIZED_JWT_VERIFY_FAILED(HttpStatus.UNAUTHORIZED, 401, "JWT 검증에 실패했습니다."),

	/**
	 * 인증 헤더가 누락됨 (HttpStatus.UNAUTHORIZED, 401, "인증 헤더가 없습니다.")
	 */
	UNAUTHORIZED_MISSING_AUTH_HEADER(HttpStatus.UNAUTHORIZED, 401, "인증 헤더가 없습니다."),

	UNAUTHORIZED_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 401, "유저명 혹은 비밀번호가 잘못되었습니다.");

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
