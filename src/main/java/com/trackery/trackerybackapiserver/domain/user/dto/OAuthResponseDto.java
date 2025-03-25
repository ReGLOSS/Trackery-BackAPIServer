package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthResponseDto
 * author         : inari
 * date           : 25. 2. 25.
 * description    : 간편 로그인 처리 결과를 담는 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.        inari       최초 생성
 * 25. 2. 26.        inari       JWT 인증 헤더 삭제
 * 25. 3. 14.        inari       이메일 추가
 */
@Getter
@Builder
public class OAuthResponseDto {

	/**
	 * 기존 이메일과 연동된 계정인지 확인합니다.
	 * 이미 데이터베이스에 존재하는 이메일이면 true, 존재하지 않으면 false를 리턴합니다.
	 */
	private boolean isExistingEmail;

	/**
	 * 사용자 이메일입니다.
	 */
	private String email;
}
