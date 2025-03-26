package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthLinkTokenDto
 * author         : inari
 * date           : 25. 3. 26.
 * description    : OAuth 계정 연동 토큰 정보를 담는 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.        inari       최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLinkTokenDto {

	/**
	 * 연동 토큰입니다.
	 */
	private String token;

	/**
	 * 간편 로그인을 등록한 SNS 이름입니다.
	 */
	private String provider;
}
