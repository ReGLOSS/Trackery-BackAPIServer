package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.config.customFilter
 * fileName       : JwtAuthenticationFilter
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.        durururuk      최초 생성
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String accessToken = (String)request.getAttribute("accessToken");

		if (accessToken != null) {
			JwtUserInfoDto userInfo = verifyAndParseJwt(accessToken);

			setAuthentication(userInfo);
		}

		filterChain.doFilter(request, response);

	}

	private JwtUserInfoDto verifyAndParseJwt(String accessToken) {
		try {
			DecodedJWT jwt = jwtService.verifyJwt(accessToken);
			Long userId = Long.valueOf(jwt.getSubject());
			String userName = jwt.getClaim("username").asString();
			Long roleId = jwt.getClaim("role").asLong();

			return new JwtUserInfoDto(userId, userName, roleId);
		} catch (JWTVerificationException e) {
			log.error("JWT 검증 실패: {}", e.getMessage());
			throw new ApiException(ErrorCode.UNAUTHORIZED_JWT_VERIFY_FAILED);
		}
	}

	private void setAuthentication(JwtUserInfoDto userInfo) {
		UserDetails userDetails = CustomUserDetails.builder()
			.userId(userInfo.userId())
			.userName(userInfo.username())
			.roleId(userInfo.roleId())
			.build();

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
			null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}
}
