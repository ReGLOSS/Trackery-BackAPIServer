package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.enums.SameSitePolicy;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
 * 25. 6. 27.        inari      	쿠키 이름과 정책 enum으로 변경
 */
@Slf4j
@RequiredArgsConstructor
public class JwtResolverFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserService userService;

	/**
	 * 필터 흐름
	 *  1. 쿠키에서 액세스 토큰을 가져옵니다.
	 *   1.1. 액세스 토큰이 없고 리프레시 토큰은 있다면 액세스 토큰을 재발급합니다.
	 *   1.2. 액세스 토큰과 리프레시 토큰이 모두 없다면 401 에러를 반환합니다.
	 *  2. 액세스 토큰을 Attribute에 담아서 필터체인을 진행합니다.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String accessToken = CookieUtil.extractCookieValue(request, CookieName.ACCESS_TOKEN.getValue(),
			() -> reissueAccessTokenByRefreshToken(request, response));

		request.setAttribute(CookieName.ACCESS_TOKEN.getValue(), accessToken);
		filterChain.doFilter(request, response);
	}

	/**
	 * 리프레시 토큰을 쿠키에서 가져옵니다. 만약 없다면 401 에러를 반환합니다.
	 * 리프레시 토큰이 있다면 액세스 토큰을 재발급해서 반환합니다.
	 *
	 * @return : 액세스 토큰
	 */
	private String reissueAccessTokenByRefreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = CookieUtil.extractCookieValue(request, CookieName.REFRESH_TOKEN.getValue(),
			() -> {
				throw new ApiException(ErrorCode.UNAUTHORIZED);
			});

		try {
			Long userId = jwtService.parseAndVerifyRefreshToken(refreshToken);

			JwtUserInfoDto jwtUserInfoDto = userService.getUserInfoById(userId);

			AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(jwtUserInfoDto.userId(),
				jwtUserInfoDto.username(), jwtUserInfoDto.roleId());

			addAuthCookiesToHeader(authTokenDto, response);

			return authTokenDto.accessToken();

		} catch (ApiException e) {
			log.error("액세스토큰 재발급 실패, ErrorCode: {}, Message: {}",
				e.getErrorCode(), e.getMessage());

			if (e.getErrorCode() == ErrorCode.UNAUTHORIZED
				|| e.getErrorCode() == ErrorCode.INTERNAL_SERVER_ERROR) {
				ResponseCookie clearCookie = CookieUtil.deleteCookie(
					CookieName.REFRESH_TOKEN.getValue(), SameSitePolicy.STRICT.getValue());
				response.addHeader("Set-Cookie", clearCookie.toString());
			}

			throw e;
		} catch (Exception e) {
			log.error("토큰 재발행 중 에러 발생", e);
			ResponseCookie clearCookie = CookieUtil.deleteCookie(
				CookieName.REFRESH_TOKEN.getValue(), SameSitePolicy.STRICT.getValue());
			response.addHeader("Set-Cookie", clearCookie.toString());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 인증에 필요한 토큰을 쿠키로 만들고 응답 헤더에 추가합니다.
	 * @param authTokenDto : 액세스 토큰과 리프레시 토큰이 담긴 DTO
	 * @param response : 응답 정보가 담긴 HttpServletResponse 객체
	 */
	private void addAuthCookiesToHeader(AuthTokenDto authTokenDto, HttpServletResponse response) {
		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.ACCESS_TOKEN.getValue(), authTokenDto.accessToken(), Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.REFRESH_TOKEN.getValue(), authTokenDto.refreshToken(), Duration.ofDays(7));

		response.addHeader("Set-Cookie", accessTokenCookie.toString());
		response.addHeader("Set-Cookie", refreshTokenCookie.toString());
	}
}
