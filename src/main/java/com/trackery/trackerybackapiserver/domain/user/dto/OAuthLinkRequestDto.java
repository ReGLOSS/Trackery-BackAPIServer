package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthLinkRequestDto
 * author         : Nari-Lee
 * date           : 25. 2. 28.
 * description    : OAuth 계정 요청 정보를 담는 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 28.		Nari-Lee		최초 생성
 * 25. 2. 28.		Nari-Lee		구현중
 * 25. 3. 26.		Nari-Lee		연동유무를 세션에서 토큰으로 이전
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLinkRequestDto {

	/**
	 * 간편 로그인을 등록한 SNS 이름입니다.
	 */
	private String provider;

	/**
	 * 사용자 이메일입니다.
	 */
	private String email;

	/**
	 * 기존 계정과 연동 여부를 확인합니다.
	 * 기존 이메일 계정과 연동 요청시 true, 새 계정 생성 또는 취소시 false를 리턴합니다.
	 */
	private boolean linkAccount;
}
