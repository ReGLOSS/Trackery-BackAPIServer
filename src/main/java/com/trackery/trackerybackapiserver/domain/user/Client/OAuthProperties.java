package com.trackery.trackerybackapiserver.domain.user.Client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.Client
 * fileName       : OAuthProperties
 * author         : inari
 * date           : 25. 2. 26.
 * description    : 간편 로그인 설정 정보를 담는 클래스입니다.
 * 					application.properties파일에서 "oauth"로 시작하는 설정값을 자동으로 바인딩합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.        inari       최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

	/**
	 * 카카오 OAuth 설정
	 */
	private ProviderProperties kakao;

	/**
	 * 네이버 OAuth 설정
	 */
	private ProviderProperties naver;

	/**
	 * 구글 OAuth 설정
	 */
	private ProviderProperties google;

	/**
	 * 깃허브 OAuth 설정
	 */
	private ProviderProperties github;

	/**
	 * OAuth 제공자의 인증에 필요한 클라이언트 정보를 저장하는 내부 클래스
	 */
	@Getter
	@Setter
	public static class ProviderProperties {
		private String clientId;
		private String clientSecret;
		private String redirectUri;
		private String tokenUri;
		private String userInfoUri;

		// 네이버 간편로그인시 필요
		private String state;
	}
}
