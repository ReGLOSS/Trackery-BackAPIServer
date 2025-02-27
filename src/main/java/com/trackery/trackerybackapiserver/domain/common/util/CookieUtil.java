package com.trackery.trackerybackapiserver.domain.common.util;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : CookieUtil
 * author         : durururuk
 * date           : 25. 2. 25.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.        durururuk      최초 생성
 * 25. 2. 26.        durururuk      액세스토큰쿠키 생성 메서드 작성
 */
public class CookieUtil {
	/**
	 * CookieUtil 클래스가 인스턴스화 됐을 때 예외를 발생시키는 생성자
	 */
	private CookieUtil() {
		throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED);
	}

	//TODO maxAge 추후 재설정, 배포 환경에서는 secure(true)로 되게 추가 필요
	/**
	 * 생성된 액세스 토큰으로 http-only 쿠키를 생성하는 메서드
	 * secure : https 연결에서만 쿠키 생성하게 설정
	 * path("/") : 모든 경로 요청에 대해 적용
	 * sameSite("Strict") : 프론트 도메인 안에서만 해당 액세스 코드 작동하게 쿠키 설정
	 *
	 * @param jwt : jwt 토큰
	 * @return : accessToken : jwt 토큰 으로 쿠키 생성
	 */
	public static ResponseCookie createAccessTokenCookie(String jwt) {
		return ResponseCookie.from("accessToken", jwt)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(Duration.ofDays(7))
			.sameSite("Strict")
			.build();
	}
}
