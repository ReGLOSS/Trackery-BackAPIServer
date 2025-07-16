package com.trackery.trackerybackapiserver.domain.common.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.enums.SameSitePolicy;
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
 * description    : JWT 토큰 기반 인증을 위한 HTTP-Only 쿠키 관리 유틸리티 클래스입니다.
 * 					Spring Security와 연동되어 안전한 JWT 토큰 관리를 제공합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 25.		durururuk		최초 생성
 * 25. 2. 26.		durururuk		액세스 토큰 쿠키 생성 메서드 클래스 분리
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 * 25. 3. 14.		durururuk		유저명 사용가능할 시 userNameToken 쿠키에 추가
 * 25. 3. 26.		durururuk		accessToken 쿠키 jwt 만료시간에 맞게 maxAge 1시간으로 수정
 * 25. 3. 27.		durururuk		액세스토큰쿠키 유지시간 jwt 만료시간과 같게 수정
 * 25. 3. 28.		durururuk		Cookie 유효시간 수정
 * 25. 4. 1.		durururuk		JwtResolverFilter에 있던 분리된 메서드들 각자 있어야 할 클래스로 이동
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 6. 23.		Nari-Lee		테스트 코드 작성
 * 25. 6. 25.		Nari-Lee		쿠키 삭제시 sameSite에 넣을 파라미터 추가
 * 25. 6. 25.		Nari-Lee		주석 추가
 * 25. 6. 27.		Nari-Lee		쿠키 이름 및 정책 enum으로 수정
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
			.sameSite(SameSitePolicy.STRICT.getValue())
			.build();
	}

	/**
	 * 쿠키를 삭제할 용도로 사용될 메서드입니다.
	 * maxAge가 0인 쿠키를 만들어서 SET-COOKIE하여 바로 삭제되게 합니다.
	 * @param key 삭제할 쿠키 key
	 * @param sameSite 쿠키의 SameSite 정책
	 * @return 삭제될 쿠키 정보
	 */
	public static ResponseCookie deleteCookie(String key, String sameSite) {
		return ResponseCookie.from(key, "")
			.httpOnly(true)
			.path("/")
			.maxAge(0)
			.sameSite(sameSite)
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

	/**
	 * 인증 토큰을 HTTP-Only 쿠키로 설정하는 메서드
	 * 액세스 토큰(60분)과 리프레시 토큰(7일) 쿠키를 생성하여 HttpHeaders에 추가합니다.
	 *
	 * @param authTokenDto 액세스 토큰과 리프레시 토큰이 포함된 DTO
	 * @return 쿠키가 설정된 HttpHeaders 객체
	 */
	public static HttpHeaders setAuthCookie(AuthTokenDto authTokenDto) {
		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.ACCESS_TOKEN.getValue(), authTokenDto.accessToken(), Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.REFRESH_TOKEN.getValue(), authTokenDto.refreshToken(), Duration.ofDays(7));

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return headers;
	}
}
