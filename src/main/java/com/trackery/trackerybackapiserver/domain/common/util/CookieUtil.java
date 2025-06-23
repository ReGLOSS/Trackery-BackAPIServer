package com.trackery.trackerybackapiserver.domain.common.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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

	/**
	 * 생성된 JWT 토큰으로 http-only 쿠키를 생성하는 메서드
	 * secure : https 연결에서만 쿠키 생성하게 설정
	 * path("/") : 모든 경로 요청에 대해 적용
	 * sameSite("Strict") : 프론트 도메인 안에서만 해당 액세스 코드 작동하게 쿠키 설정
	 *
	 * @param key : 쿠키 이름
	 * @param value : 쿠키 값
	 * @param duration : 쿠키의 유효시간
	 * @return : 생성된 http-only 쿠키
	 */
	public static ResponseCookie createHttpOnlyCookie(String key, String value, Duration duration) {
		return ResponseCookie.from(key, value)
			.httpOnly(true)
			.path("/")
			//TODO JWT 토큰 만료시간 일괄 관리되게 수정
			.maxAge(duration)
			.sameSite("Strict")
			.build();
	}

	/**
	 * 쿠키를 삭제할 용도로 사용될 메서드입니다.
	 * maxAge가 0인 쿠키를 만들어서 SET-COOKIE하여 바로 삭제되게 합니다.
	 * @param key 삭제할 쿠키 key
	 * @return 삭제될 쿠키 정보
	 */
	public static ResponseCookie deleteCookie(String key) {
		return ResponseCookie.from(key, "")
			.httpOnly(true)
			.path("/")
			.maxAge(0)
			.sameSite("Strict")
			.build();
	}

	/**
	 * 요청 객체에서 쿠키를 가져와서 원하는 쿠키 값을 반환합니다.
	 * 찾지 못했을 경우 Supplier를 실행해서 대체 값을 반환합니다.
	 *
	 * @param request : HttpServletRequest 요청 객체, 여기서 쿠키를 가져옵니다.
	 * @param cookieName : 가져오고자 하는 쿠키의 key값
	 * @param ifAbsent : 쿠키를 가져오지 못했을 경우 실행될 람다 메서드
	 * @return : 가져온 쿠키 값 혹은 Supplier에서 받아온 대체 값
	 */
	public static String extractCookieValue(HttpServletRequest request, String cookieName, Supplier<String> ifAbsent) {
		return Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> cookieName.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue))
			.orElseGet(ifAbsent);
	}

	public static HttpHeaders setAuthCookie(AuthTokenDto authTokenDto) {
		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie("accessToken", authTokenDto.accessToken(),
			Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie("refreshToken", authTokenDto.refreshToken(),
			Duration.ofDays(7));

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return headers;
	}
}
