package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtRedisService;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.config
 * fileName       : JwtFilter
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : JWT 인증/인가를 담당하는 필터
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.        durururuk       최초 생성
 */
@Slf4j
@RequiredArgsConstructor
public class JwtResolverFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final JwtRedisService jwtRedisService;
	private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

	/**
	 * JWT 인증/인가를 해주는 필터
	 * 1. 인증 헤더가 올바르지 않으면 예외 처리
	 * 2. jwt 토큰 파싱, 검증
	 * 3. 인증 정보를 SecurityContext에 담고 필터 통과
	 *
	 * @param request : 클라이언트의 http 요청 객체
	 * @param response : 서버의 http 응답 객체
	 * @param filterChain : 다음 필터 혹은 리소스로 넘겨주는 필터체인 객체
	 * @throws ServletException : 필터 처리 중 발생할 수 있는 서블릿 관련 예외
	 * @throws IOException : 입출력 처리 중 발생할 수 있는 예외
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String accessToken = getAccessTokenFromCookie(request);

		request.setAttribute(ACCESS_TOKEN_COOKIE_NAME, accessToken);
		filterChain.doFilter(request, response);
	}

	private String getAccessTokenFromCookie(HttpServletRequest request) {
		return Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue))
			.orElseGet(() -> reissueAccessTokenViaRefreshToken(request));
	}

	private String reissueAccessTokenViaRefreshToken(HttpServletRequest request) {
		String refreshToken = Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue))
			.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

		return jwtService.reissueAccessTokenByRefreshToken(refreshToken);
	}
}
