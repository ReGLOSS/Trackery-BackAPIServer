package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthUrlResponseDto
 * author         : inari
 * date           : 25. 6. 23.
 * description    : OAuth 인증 URL 응답 DTO 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 23.        inari       최초 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUrlResponseDto {

	/**
	 * OAuth 인증 URL
	 */
	private String authUrl;

	/**
	 * OAuth 제공자
	 */
	private String provider;

	/**
	 * 상태 값 (선택사항)
	 */
	private String state;
}
