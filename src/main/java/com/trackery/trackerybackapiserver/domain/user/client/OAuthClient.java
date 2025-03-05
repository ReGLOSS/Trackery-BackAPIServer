package com.trackery.trackerybackapiserver.domain.user.client;

import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.TokenResponseDto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.client
 * fileName       : OAuthClient
 * author         : inari
 * date           : 25. 2. 26.
 * description    : 간편 로그인 제공자와 통신하기 위한 클라이언트 인터페이스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.        inari       최초 생성
 */
public interface OAuthClient {

	/**
	 * 인증 코드로 액세스 토큰과 리프레시 토큰을 함께 획득하는 메서드
	 *
	 * @param code 일회성 인증 코드
	 * @param provider 간편 로그인 제공자
	 * @return 액세스 토큰과 리프레시 토큰을 포함한 응답
	 */
	TokenResponseDto getTokens(String code, String provider);

	/**
	 * 인증 코드로 액세스 토큰을 획득하는 메서드
	 *
	 * @param code 일회성 인증 코드
	 * @param provider 간편 로그인 제공자
	 * @return 액세스 토큰
	 */
	String getAccessToken(String code, String provider);

	/**
	 * 액세스 토큰으로 사용자 정보를 획득하는 메서드
	 *
	 * @param accessToken 액세스 토큰
	 * @param provider 간편 로그인 제공자
	 * @return 사용자 정보
	 */
	OAuthUserInfoDto getUserInfo(String accessToken, String provider);
}
