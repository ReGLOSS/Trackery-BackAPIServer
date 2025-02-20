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

	private boolean isPublicUri(String uri) {
		return uri.startsWith("/error") || uri.startsWith("/api/users/register") || uri.startsWith(
			"/api/users/exists/username");
	}

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
