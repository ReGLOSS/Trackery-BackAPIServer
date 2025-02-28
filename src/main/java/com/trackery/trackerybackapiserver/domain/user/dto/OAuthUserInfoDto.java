package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.dto
 * fileName       : OAuthUserInfoDto
 * author         : inari
 * date           : 25. 2. 24.
 * description    : 간편 로그인 정보를 담는 DTO 클래스입니다.
 * 					이 클래스틑 간편 로그인을 이용한 회원가입시 프로바이더에서 제공한 정보를 전달합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 24.        inari       최초 생성
 */
@Getter
@Builder
public class OAuthUserInfoDto {

	/**
	 * 사용자 이메일입니다.
	 */
	private String email;

	/**
	 * 사용자의 닉네임입니다.
	 */
	private String nickname;

	/**
	 * 사용자의 프로필 사진입니다.
	 */
	private String userprofile;

	/**
	 * 간편 로그인을 등록한 SNS 이름입니다.
	 */
	private String provider;

	/**
	 * 간편 로그인을 등록한 SNS에서 제공한 사용자의 고유 식별자입니다.
	 */
	private String providerUserId;
}
