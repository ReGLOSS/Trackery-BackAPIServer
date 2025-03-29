package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtRedisService;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

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
 * description    : 액세스 토큰을 가져오는 필터입니다
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
	private final UserService userService;
	private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

	/**
	 * 필터 흐름
	 *  1. 쿠키에서 액세스 토큰을 가져옵니다.
	 *   1.1. 액세스 토큰이 없고 리프레시 토큰은 있다면 액세스 토큰을 재발급합니다.
	 *   1.2. 액세스 토큰과 리프레시 토큰이 모두 없다면 401 에러를 반환합니다.
	 *
	 *  2. 액세스 토큰을 Attribute에 담아서 필터체인을 진행합니다.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String accessToken = getAccessTokenFromCookie(request, response);

		request.setAttribute(ACCESS_TOKEN_COOKIE_NAME, accessToken);
		filterChain.doFilter(request, response);
	}

	/**
	 * 액세스 토큰을 쿠키에서 가져옵니다.
	 * 만약 액세스 토큰이 없다면 리프레시 토큰을 가져오는 메서드로 진행됩니다.
	 *
	 * @return : 액세스 토큰
	 */
	private String getAccessTokenFromCookie(HttpServletRequest request, HttpServletResponse response) {
		return Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue))
			.orElseGet(() -> reissueAccessTokenViaRefreshToken(request, response));
	}

	/**
	 * 리프레시 토큰을 쿠키에서 가져옵니다. 만약 없다면 401 에러를 반환합니다.
	 * 리프레시 토큰이 있다면 액세스 토큰을 재발급해서 반환합니다.
	 *
	 * @return : 액세스 토큰
	 */
	private String reissueAccessTokenViaRefreshToken(HttpServletRequest request, HttpServletResponse response) {
		//쿠키에서 리프레시 토큰을 가져옵니다. 만약 없다면 401 에러를 반환합니다.
		String refreshToken = Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.findFirst()
				.map(Cookie::getValue))
			.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));


		//리프레시 토큰을 파싱하고 서버에 저장된 리프레시 토큰의 정보와 일치하는지 대조합니다.
		DecodedJWT decodedRefreshToken = jwtService.verifyJwt(refreshToken);
		RefreshTokenDto refreshTokenDto = jwtRedisService.getRefreshTokenInfo(refreshToken);

		if (!decodedRefreshToken.getId().equals(refreshTokenDto.jid())) {
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}

		Long userId = Long.valueOf(refreshTokenDto.subject());
		JwtUserInfoDto jwtUserInfoDto = userService.getUserInfoById(userId);

		jwtRedisService.deleteRefreshToken(refreshToken);

		//액세스 토큰 재발급해서 쿠키에 담고 HttpServletResponse header에 추가합니다.
		AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(jwtUserInfoDto.userId(),
			jwtUserInfoDto.username(), jwtUserInfoDto.roleId());

		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie(ACCESS_TOKEN_COOKIE_NAME,
			authTokenDto.accessToken(),
			Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie(REFRESH_TOKEN_COOKIE_NAME,
			authTokenDto.refreshToken(),
			Duration.ofDays(7));

		response.addHeader("Set-Cookie", accessTokenCookie.toString());
		response.addHeader("Set-Cookie", refreshTokenCookie.toString());

		return authTokenDto.accessToken();
	}
}
