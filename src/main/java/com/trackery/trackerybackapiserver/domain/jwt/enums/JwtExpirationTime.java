package com.trackery.trackerybackapiserver.domain.jwt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.enums
 * fileName       : JwtExpirationTime
 * author         : durururuk
 * date           : 25. 6. 19.
 * description    : JWT 토큰 만료 시간을 관리하는 enum입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 19.		durururuk		최초 생성
 * 25. 6. 19.		durururuk		JWT 토큰 만료시간 enum으로 관리하게 수정
 * 25. 6. 19.		durururuk		기존 액세스 토큰의 시간 1시간을 그대로 가져오던 이메일 인증 토큰, 유저명 중복 확인 토큰을 각각 처리하게 수정
 */
@Getter
@AllArgsConstructor
public enum JwtExpirationTime {
	ACCESS_TOKEN(60 * 60),
	REFRESH_TOKEN(60 * 60 * 24 * 7),
	MAIL_VERIFICATION_TOKEN(60 * 60),
	USER_NAME_VERIFICATION_TOKEN(60 * 60);

	private final int expirationTime;
}
