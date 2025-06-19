package com.trackery.trackerybackapiserver.domain.jwt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.enums
 * fileName       : EXPIRATION_TIME
 * author         : durururuk
 * date           : 25. 6. 19.
 * description    : JWT 토큰 만료 시간을 관리하는 enum입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 19.		durururuk		최초 생성
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
