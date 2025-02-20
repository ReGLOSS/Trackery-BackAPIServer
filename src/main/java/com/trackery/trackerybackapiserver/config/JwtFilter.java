package com.trackery.trackerybackapiserver.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *packageName    : com.trackery.trackerybackapiserver.config

 fileName       : JwtFilter
 author         : durururuk
 date           : 25. 2. 19.
 description    : JWT 인증/인가를 담당하는 필터
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;

	/**
	 * JWT 인증/인가 필요없는 퍼블릭 uri를 모아서 boolean을 반환하는 메서드
	 * @param uri 퍼블릭 uri
	 * @return 퍼블릭 uri면 true, 아니라면 false
	 */
	private boolean isPublicUri(String uri) {
		return uri.startsWith("/error")
			|| uri.startsWith("/api/users/register")
			|| uri.startsWith("/api/users/exists/username");
	}

	/**
	 * JWT 인증/인가를 해주는 필터
	 *
	 * 1. 요청 uri가 퍼블릭이라면 필터 통과
	 * 2. 인증 헤더가 올바르지 않으면 예외 처리
	 * 3. jwt 토큰 파싱, 검증
	 * 4. 인증 정보를 SecurityContext에 담고 필터 통과
	 *
	 * @param request : 클라이언트의 http 요청 객체
	 * @param response : 서버의 http 응답 객체
	 * @param filterChain : 다음 필터 혹은 리소스로 넘겨주는 필터체인 객체
	 * @throws ServletException : 필터 처리 중 발생할 수 있는 서블릿 관련 예외
	 * @throws IOException : 입출력 처리 중 발생할 수 있는 예외
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		if (isPublicUri(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = Optional.ofNullable(request.getHeader("Authorization"))
			.filter(header -> header.startsWith("Bearer "))
			.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED_MISSING_AUTH_HEADER));

		String token = authHeader.substring(7);
		DecodedJWT jwt = jwtUtil.verifyJwt(token);

		Long userId = Long.valueOf(jwt.getSubject());
		Long roleId = jwt.getClaim("roleId").asLong();

		UserDetails userDetails = CustomUserDetails.builder().userId(userId).roleId(roleId).build();

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
			null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		filterChain.doFilter(request, response);
	}
}
