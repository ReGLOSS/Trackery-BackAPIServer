package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthLoginDto
 * author         : inari
 * date           : 25. 2. 25.
 * description    : 간편 로그인 요청 정보를 담는 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.        inari       최초 생성
 */
@Getter
@Builder
public class OAuthLoginDto {

	/**
	 * 간편 로그인을 등록한 SNS 이름입니다.
	 */
	private String provider;

	/**
	 * 간편 로그인 인증 코드입니다.
	 * OAuth 인증 과정에서 받은 인증 코드로, 액세스 토큰 교환에 사용됩니다.
	 */
	private String code;

	/**
	 * 기존 계정과 연동 여부를 확인합니다.
	 * 기존 이메일 계정과 연동 요청시 true, 새 계정 생성 또는 취소시 false를 리턴합니다.
	 */
	private boolean linkAccount;

	/**
	 * 연동할 사용자 ID입니다.
	 * link_token에서 추출한 사용자 ID로, 계정 연동 시 사용됩니다.
	 */
	private Long linkUserId;
}
