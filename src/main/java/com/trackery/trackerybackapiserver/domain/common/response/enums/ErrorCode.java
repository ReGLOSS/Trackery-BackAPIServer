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
 * 25. 2. 13.		durururuk		최초 생성
 * 25. 2. 13.		durururuk		기본 응답 포맷 작성
 * 25. 2. 14.		durururuk		Validation 실패 예외, 잘못된 RequestBody 예외 처리하는 ErrorCode, 핸들러 수정
 * 25. 2. 14.		durururuk		회원가입 할 때 비밀번호를 해싱해서 저장하게 수정
 * 25. 2. 17.		inari		공통응답 테스트코드 추가
 * 25. 2. 18.		durururuk		JWT 토큰 추가 메서드 추가
 * 25. 2. 19.		inari		이미지가 비었을시 에러코드로 변경
 * 25. 2. 19.		durururuk		UserDetails, UserDetailsService 구현체 작성,
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 2. 25.		durururuk		userRole 매퍼 추가, userMapper에 있던 userRoleInsert 이동
 * 25. 2. 28.		inari		구현중
 * 25. 3. 20.		inari		getUserInfo의 복잡도 15이하로 변경
 * 25. 3. 26.		inari		토큰 생성시 검증부분 추가
 * 25. 3. 31.		Durururuk		refresh-token/ ENUM 컨벤션 수정
 * 25. 4. 9.		durururuk		에러코드 상세하게 변경
 * 25. 4. 10.		durururuk		에러코드 수정
 * 25. 4. 12.		durururuk		닉네임 변경 기능 구현
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 5. 6.		durururuk		이미지 업로드 시 객체 존재 여부 확인 로직 추가
 * 25. 5. 15.		durururuk		이미지 조회 기능 폴더 지정 오류로 안 되던 문제 수정
 * 25. 5. 21.		durururuk		앨범에 이미지 추가하는 기능 작성
 * 25. 5. 30.		inari		제작중
 * 25. 6. 20.		inari		pr 코멘트받은 내용 수정
 * 25. 6. 24.		inari		에러코드 및 필요한 설정 추가
 * 25. 6. 27.		inari		에러코드 정리
 * 25. 6. 27.		inari		이미 연동된 간편로그인 타유저 접근 차단
 * 25. 7. 7.		inari		프로퍼티 에러코드 추가
 * 25. 7. 10.		durururuk		PageUtil convert 메서드 null 체크 추가
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

	/*
	 * =================================================================================================================
	 * 공통 및 일반 오류 (Common & General Errors)
	 * =================================================================================================================
	 */
	/**
	 * 잘못된 요청 (HttpStatus.BAD_REQUEST, 400, "Bad Request")
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),
	/**
	 * 인증되지 않은 요청 (HttpStatus.UNAUTHORIZED, 401, "Unauthorized")
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


	/*
	 * =================================================================================================================
	 * 사용자 및 인증 관련 오류 (User & Authentication Errors)
	 * =================================================================================================================
	 */
	/**
	 * 사용자를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "사용자를 찾을 수 없습니다.")
	 */
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "사용자를 찾을 수 없습니다."),
	/**
	 * 로그인 자격 증명이 잘못됨 (HttpStatus.BAD_REQUEST, 400, "유저명 혹은 비밀번호가 잘못되었습니다.")
	 */
	BAD_REQUEST_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, 400, "유저명 혹은 비밀번호가 잘못되었습니다."),
	/**
	 * 비밀번호 불일치 (HttpStatus.BAD_REQUEST, 400, "비밀번호가 일치하지 않습니다.")
	 */
	BAD_REQUEST_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 400, "비밀번호가 일치하지 않습니다."),
	/**
	 * 유효하지 않은 사용자 인증 정보 (HttpStatus.BAD_REQUEST, 400, "유효하지 않은 사용자 인증입니다.")
	 */
	BAD_REQUEST_INVALID_USER_AUTH(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 사용자 인증입니다."),
	/**
	 * 이미 데이터베이스에 존재하는 이메일 (HttpStatus.CONFLICT, 409, "이미 가입된 이메일입니다.")
	 */
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, 409, "이미 가입된 이메일입니다."),


	/*
	 * =================================================================================================================
	 * OAuth 및 소셜 로그인 관련 오류 (OAuth & Social Login Errors)
	 * =================================================================================================================
	 */
	/**
	 * OAuth 인증에 실패 (HttpStatus.UNAUTHORIZED, 401, "OAuth 인증에 실패했습니다.")
	 */
	UNAUTHORIZED_OAUTH_FAILED(HttpStatus.UNAUTHORIZED, 401, "OAuth 인증에 실패했습니다."),
	/**
	 * 지원하지 않는 OAuth 제공자 (HttpStatus.BAD_REQUEST, 400, "지원하지 않는 OAuth 제공자입니다.")
	 */
	BAD_REQUEST_INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, 400, "지원하지 않는 OAuth 제공자입니다."),
	/**
	 * GitHub 계정이 미공개라 이메일을 찾을 수 없음 (HttpStatus.BAD_REQUEST, 400, "이메일은 필수 정보입니다. GitHub 계정에 공개 이메일을 설정해주세요.")
	 */
	BAD_REQUEST_INVALID_INPUT_GITHUB(HttpStatus.BAD_REQUEST, 400, "이메일은 필수 정보입니다. GitHub 계정에 공개 이메일을 설정해주세요."),
	/**
	 * 이미 다른 계정에 연동된 소셜 계정 (HttpStatus.CONFLICT, 409, "이미 다른 계정에 연동된 소셜 계정입니다.")
	 */
	CONFLICT_OAUTH_ALREADY_LINKED(HttpStatus.CONFLICT, 409, "이미 다른 계정에 연동된 소셜 계정입니다."),


	/*
	 * =================================================================================================================
	 * JWT 관련 오류 (JWT Errors)
	 * =================================================================================================================
	 */
	/**
	 * 인증 헤더가 누락됨 (HttpStatus.UNAUTHORIZED, 401, "인증 헤더가 없습니다.")
	 */
	UNAUTHORIZED_MISSING_AUTH_HEADER(HttpStatus.UNAUTHORIZED, 401, "인증 헤더가 없습니다."),
	/**
	 * JWT 검증 실패 (HttpStatus.UNAUTHORIZED, 401, "JWT 검증에 실패했습니다.")
	 */
	UNAUTHORIZED_JWT_VERIFY_FAILED(HttpStatus.UNAUTHORIZED, 401, "JWT 검증에 실패했습니다."),
	/**
	 * JWT 토큰 생성 실패 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "JWT 토큰 생성에 실패했습니다.")
	 */
	INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT(HttpStatus.INTERNAL_SERVER_ERROR, 500, "JWT 토큰 생성에 실패했습니다."),


	/*
	 * =================================================================================================================
	 * 요청 유효성 검사 및 데이터 관련 오류 (Request Validation & Data Errors)
	 * =================================================================================================================
	 */
	/**
	 * 요청 데이터 유효성 검사 실패 (HttpStatus.BAD_REQUEST, 400, "Validation 실패")
	 */
	BAD_REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "Validation 실패"),
	/**
	 * 잘못된 요청 본문 (HttpStatus.BAD_REQUEST, 400, "잘못된 Request Body")
	 */
	BAD_REQUEST_INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, 400, "잘못된 Request Body"),
	/**
	 * 이메일이 존재하지 않을 경우 (HttpStatus.BAD_REQUEST, 400, "이메일은 필수 정보입니다.")
	 */
	BAD_REQUEST_INVALID_INPUT_MISSING_EMAIL(HttpStatus.BAD_REQUEST, 400, "이메일은 필수 정보입니다."),
	/**
	 * 동일한 내용으로 업데이트 시도 (HttpStatus.BAD_REQUEST, 400, "같은 내용으로는 변경할 수 없습니다.")
	 */
	BAD_REQUEST_SAME_UPDATE(HttpStatus.BAD_REQUEST, 400, "같은 내용으로는 변경할 수 없습니다."),


	/*
	 * =================================================================================================================
	 * 이미지 및 S3 관련 오류 (Image & S3 Errors)
	 * =================================================================================================================
	 */
	/**
	 * 요청한 이미지를 불러올 수 없음 (HttpStatus.NOT_FOUND, 404, "이미지를 찾지 못했습니다.")
	 */
	NOT_FOUND_IMAGE(HttpStatus.NOT_FOUND, 404, "이미지를 찾지 못했습니다."),
	/**
	 * S3에서 이미지 객체를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "S3에서 해당 이미지를 찾을 수 없습니다.")
	 */
	NOT_FOUND_IMAGE_OBJECT_KEY(HttpStatus.NOT_FOUND, 404, "S3에서 해당 이미지를 찾을 수 없습니다."),
	/**
	 * 허용되지 않은 이미지 파일 형식 (HttpStatus.BAD_REQUEST, 400, "허용된 이미지 파일이 아닙니다.")
	 */
	BAD_REQUEST_INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, 400, "허용된 이미지 파일이 아닙니다."),
	/**
	 * 데이터베이스에서 이미지 URL을 조회하는 중 오류 발생 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 URL 조회중 오류가 발생했습니다.")
	 */
	INTERNAL_SERVER_ERROR_LOAD_DATABASE(HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 URL 조회중 오류가 발생했습니다."),
	/**
	 * 이미지 메타데이터 업데이트 실패 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 메타데이터 업데이트에 실패했습니다. 다른 요청에 의해 리소스가 변경되었을 수 있습니다.")
	 */
	UPDATE_FAILED_META(HttpStatus.INTERNAL_SERVER_ERROR, 500,
		"이미지 메타데이터 업데이트에 실패했습니다. 다른 요청에 의해 리소스가 변경되었을 수 있습니다."),
	/**
	 * 이미지 위치 정보 업데이트 실패 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "이미지 위치 정보 업데이트에 실패했습니다. 다른 요청에 의해 리소스가 변경되었을 수 있습니다.")
	 */
	UPDATE_FAILED_LOCATION(HttpStatus.INTERNAL_SERVER_ERROR, 500,
		"이미지 위치 정보 업데이트에 실패했습니다. 다른 요청에 의해 리소스가 변경되었을 수 있습니다."),


	/*
	 * =================================================================================================================
	 * 위치 정보 관련 오류 (Location Errors)
	 * =================================================================================================================
	 */
	/**
	 * 시도를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "시도를 찾을 수 없습니다.")
	 */
	NOT_FOUND_SIDO(HttpStatus.NOT_FOUND, 404, "시도를 찾을 수 없습니다."),
	/**
	 * 시군구를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "시군구를 찾을 수 없습니다.")
	 */
	NOT_FOUND_SIGUNGU(HttpStatus.NOT_FOUND, 404, "시군구를 찾을 수 없습니다."),
	/**
	 * 유효하지 않은 좌표 (HttpStatus.BAD_REQUEST, 400, "유효하지 않은 좌표입니다.")
	 */
	BAD_REQUEST_INVALID_COORDINATE(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 좌표입니다."),


	/*
	 * =================================================================================================================
	 * 도메인별 특화 오류 (Domain-Specific Errors)
	 * =================================================================================================================
	 */
	/**
	 * 앨범을 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "앨범을 찾지 못했습니다.")
	 */
	NOT_FOUND_ALBUM(HttpStatus.NOT_FOUND, 404, "앨범을 찾지 못했습니다."),


	/*
	 * =================================================================================================================
	 * 페이지네이션 관련 오류 (Pagination Errors)
	 * =================================================================================================================
	 */
	/**
	 * 페이지 정보 변환 시 원본 페이지 정보가 null일 경우 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "PageInfo 변환 중 오류 발생, Source PageInfo가 null입니다.")
	 */
	INTERNAL_SERVER_ERROR_PAGE_SOURCE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, 500,
		"PageInfo 변환 중 오류 발생, Source PageInfo가 null입니다."),
	/**
	 * 페이지 정보 변환 시 새로운 리스트가 null일 경우 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "PageInfo 변환 중 오류 발생, 변환될 새 리스트가 null입니다.")
	 */
	INTERNAL_SERVER_ERROR_PAGE_NEW_LIST_NULL(HttpStatus.INTERNAL_SERVER_ERROR, 500,
		"PageInfo 변환 중 오류 발생, 변환될 새 리스트가 null입니다."),
	/**
	 * 페이지 정보 변환 시 변환 함수가 null일 경우 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "PageInfo 변환 중 오류 발생, 변환 메서드가 null입니다.")
	 */
	INTERNAL_SERVER_ERROR_PAGE_CONVERTER_NULL(HttpStatus.INTERNAL_SERVER_ERROR, 500,
		"PageInfo 변환 중 오류 발생, 변환 메서드가 null입니다."),

	/*
	 * =================================================================================================================
	 * 태그 관련 오류 (Tag Errors)
	 * =================================================================================================================
	 */
	/**
	 * 태그를 찾을 수 없음 (HttpStatus.NOT_FOUND, 404, "태그를 찾을 수 없습니다.")
	 */
	NOT_FOUND_TAG(HttpStatus.NOT_FOUND, 404, "태그를 찾을 수 없습니다."),
	/**
	 * 중복된 태그 이름 (HttpStatus.CONFLICT, 409, "이미 존재하는 태그 이름입니다.")
	 */
	DUPLICATE_TAG_NAME(HttpStatus.CONFLICT, 409, "이미 존재하는 태그 이름입니다."),
	/**
	 * 시스템 태그 삭제 불가 (HttpStatus.BAD_REQUEST, 400, "시스템 태그는 삭제할 수 없습니다.")
	 */
	BAD_REQUEST_CANNOT_DELETE_SYSTEM_TAG(HttpStatus.BAD_REQUEST, 400, "시스템 태그는 삭제할 수 없습니다."),
	/**
	 * 사용 중인 태그 삭제 불가 (HttpStatus.BAD_REQUEST, 400, "사용 중인 태그는 삭제할 수 없습니다.")
	 */
	BAD_REQUEST_CANNOT_DELETE_TAG_IN_USE(HttpStatus.BAD_REQUEST, 400, "사용 중인 태그는 삭제할 수 없습니다."),
	/**
	 * 사용자가 사용하지 않는 태그 (HttpStatus.BAD_REQUEST, 400, "사용자가 사용하지 않는 태그입니다.")
	 */
	BAD_REQUEST_TAG_NOT_USED_BY_USER(HttpStatus.BAD_REQUEST, 400, "사용자가 사용하지 않는 태그입니다."),


	/*
	 * =================================================================================================================
	 * 서버 내부 시스템 오류 (Internal System Errors)
	 * =================================================================================================================
	 */
	/**
	 * SHA-256 알고리즘을 사용할 수 없음 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "SHA-256 알고리즘을 사용할 수 없습니다.")
	 */
	INTERNAL_SERVER_ERROR_NO_SUCH_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, 500, "SHA-256 알고리즘을 사용할 수 없습니다."),
	/**
	 * 유틸리티 클래스의 객체가 생성됨 (HttpStatus.INTERNAL_SERVER_ERROR, 500, "유틸리티 클래스 객체가 생성되었습니다.")
	 */
	INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "유틸리티 클래스 객체가 생성되었습니다.");

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
