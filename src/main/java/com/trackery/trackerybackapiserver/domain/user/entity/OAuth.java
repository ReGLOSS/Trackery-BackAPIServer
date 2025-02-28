package com.trackery.trackerybackapiserver.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.entity
 * fileName       : OAuth
 * author         : inari
 * date           : 25. 2. 24.
 * description    : 유저 간편 로그인 정보를 담는 엔티티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 24.        inari       최초 생성
 * 25. 2. 28.        inari       리프레시 토큰 제거
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth {

	/**
	 * 간편 로그인 고유 식별자
	 */
	private Long oauthId;

	/**
	 * 사용자의 고유 식별자
	 */
	private Long userId;

	/**
	 * 간편 로그인을 등록한 SNS 이름입니다.
	 */
	private String provider;

	/**
	 * 간편 로그인을 등록한 SNS에서 제공한 사용자의 고유 식별자입니다.
	 */
	private String providerUserId;

	@Builder
	public OAuth(Long userId, String provider, String providerUserId) {
		this.userId = userId;
		this.provider = provider;
		this.providerUserId = providerUserId;
	}
}
