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
 * packageName    : com.trackery.trackerybackapiserver.config.filter
 * fileName       : JwtResolverFilter
 * author         : durururuk
 * date           : 25. 2. 19.
 * description    : 액세스 토큰을 가져오는 필터입니다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 19.		durururuk		최초 생성
 * 25. 2. 19.		durururuk		Jwt 적용을 위한 필터 작성, 추가
 * 25. 2. 19.		durururuk		Jwt 생성 시 역할 정보를 담도록 추가, jwt 필터에도 반영, 로직 개선
 * 25. 2. 20.		durururuk		회원가입 시 JWT를 담은 헤더를 같이 반환하도록 추가
 * 25. 2. 20.		durururuk		주석 추가
 * 25. 2. 21.		durururuk		CustomWebSecurityConfig으로 퍼블릭 uri 관리 일원화
 * 25. 2. 21.		durururuk		사용자명 중복체크 메서드명 더 명확하게 수정, 반대로 작동하던 로직 수정
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 3. 14.		durururuk		예외처리 필터 작성
 * 25. 3. 27.		durururuk		CustomUserDetails에서 userName도 함께 담도록 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		JwtFilter 메서드 분리
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 28.		durururuk		필터 순서 수정
 * 25. 3. 28.		durururuk		리프레쉬 토큰으로 액세스 토큰 재발급 시, 쿠키에 다시 추가되게 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 재발급 시 기존 리프레시 토큰 레디스에서 삭제되게 수정
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 3. 29.		durururuk		주석 작성
 * 25. 4. 1.		Durururuk		JwtResolverFilter 메서드 분리
 * 25. 4. 1.		Durururuk		JwtResolverFilter에 있던 분리된 메서드들 각자 있어야 할 클래스로 이동
 * 25. 4. 1.		durururuk		Bean 순환 문제 해결
 * 25. 6. 27.		inari		쿠키 이름 및 정책 enum으로 수정
 * 25. 7. 9.		durururuk		리프레시 토큰으로 액세스토큰 재발급시 예외처리 강화 및 로깅 추가
 * 25. 7. 14.		durururuk		액세스토큰이 쿠키에 있지만 만료된 경우 예외 처리 추가
 * 25. 7. 15.		inari		"Set-Cookie" 상수로 전환
 */
@Slf4j
@RequiredArgsConstructor
public class JwtResolverFilter extends OncePerRequestFilter {

	public static final String SET_COOKIE = "Set-Cookie";
	private final JwtService jwtService;
	private final UserService userService;

	/**
	 * 필터 흐름
	 *  1. 쿠키에서 액세스 토큰을 가져옵니다.
	 *   1.1. 액세스 토큰이 없고 리프레시 토큰은 있다면 액세스 토큰을 재발급합니다.
	 *   1.2. 액세스 토큰이 있지만 만료되었다면 리프레시 토큰으로 재발급합니다.
	 *   1.3. 액세스 토큰과 리프레시 토큰이 모두 없다면 401 에러를 반환합니다.
	 *  2. 액세스 토큰을 Attribute에 담아서 필터체인을 진행합니다.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String accessToken = getValidAccessToken(request, response);

		request.setAttribute(CookieName.ACCESS_TOKEN.getValue(), accessToken);
		filterChain.doFilter(request, response);
	}

	/**
	 * 유효한 액세스 토큰을 가져오는 메서드
	 * 액세스 토큰이 없거나 만료된 경우 리프레시 토큰으로 재발급합니다.
	 *
	 * @param request : HttpServletRequest 요청 객체
	 * @param response : HttpServletResponse 응답 객체
	 * @return : 유효한 액세스 토큰
	 */
	private String getValidAccessToken(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = CookieUtil.extractCookieValue(request, CookieName.ACCESS_TOKEN.getValue(), () -> null);

		if (accessToken != null) {
			try {
				jwtService.verifyJwt(accessToken);
				return accessToken;
			} catch (ApiException e) {
				if (e.getErrorCode() == ErrorCode.BAD_REQUEST || e.getErrorCode() == ErrorCode.UNAUTHORIZED) {
					log.debug("액세스 토큰이 만료되었습니다. 리프레시 토큰으로 재발급을 시도합니다.");
					return reissueAccessTokenByRefreshToken(request, response);
				}
				throw e;
			}
		}

		return reissueAccessTokenByRefreshToken(request, response);
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
				response.addHeader(SET_COOKIE, clearCookie.toString());
			}

			throw e;
		} catch (Exception e) {
			log.error("토큰 재발행 중 에러 발생", e);
			ResponseCookie clearCookie = CookieUtil.deleteCookie(
				CookieName.REFRESH_TOKEN.getValue(), SameSitePolicy.STRICT.getValue());
			response.addHeader(SET_COOKIE, clearCookie.toString());
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

		response.addHeader(SET_COOKIE, accessTokenCookie.toString());
		response.addHeader(SET_COOKIE, refreshTokenCookie.toString());
	}
}
