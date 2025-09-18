package com.trackery.trackerybackapiserver.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtRedisService;
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
 * packageName    : com.trackery.trackerybackapiserver.config.filter
 * fileName       : JwtAuthenticationFilter
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    : 액세스 토큰을 검증하고 파싱해서 유저 인증 정보를 시큐리티 컨텍스트 홀더에 저장합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.		durururuk		최초 생성
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		JwtFilter 메서드 분리
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 28.		durururuk		필터 순서 수정
 * 25. 3. 28.		durururuk		리프레쉬 토큰으로 액세스 토큰 재발급 시, 쿠키에 다시 추가되게 수정
 * 25. 3. 29.		durururuk		주석 작성
 * 25. 6. 27.		inari		쿠키 이름 및 정책 enum으로 수정
 * 25. 9. 18.		inari		사용자 정지 기능 추가
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final JwtRedisService jwtRedisService;

	/**
	 * 필터 흐름
	 *  1. Attribute에서 액세스 토큰을 받아옵니다.
	 *  2. 액세스 토큰을 검증하고 파싱해서 유저 정보를 가져옵니다.
	 *  3. 가져온 유저 정보를 스프링 시큐리티 컨텍스트 홀더에 저장합니다.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String accessToken = (String)request.getAttribute(CookieName.ACCESS_TOKEN.getValue());

		if (accessToken == null) {
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}

		JwtUserInfoDto userInfo = verifyAndParseJwt(accessToken);
		checkUserSuspension(userInfo.userId());
		setAuthentication(userInfo);

		filterChain.doFilter(request, response);

	}

	/**
	 * 액세스 토큰 검증, 파싱, DTO로 변환해서 반환하는 메서드
	 * @param accessToken : 액세스 토큰
	 * @return : userId, userName, roleID를 담고있는 DTO
	 */
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

	/**
	 * 사용자 정지 상태를 확인하는 메서드
	 * @param userId : 확인할 사용자 ID
	 */
	private void checkUserSuspension(Long userId) {
		if (jwtRedisService.isUserSuspended(userId)) {
			String suspensionInfo = jwtRedisService.getUserSuspensionInfo(userId);
			if (suspensionInfo != null) {
				String[] parts = suspensionInfo.split(":");
				Integer suspensionType = Integer.valueOf(parts[0]);

				if (suspensionType == 2) {
					String endDate = parts.length > 1 ? parts[1] : "";
					log.warn("임시정지된 사용자 접근 시도: userId={}, endDate={}", userId, endDate);
					throw new ApiException(ErrorCode.FORBIDDEN_ACCOUNT_TEMPORARILY_SUSPENDED);
				} else if (suspensionType == 3) {
					log.warn("영구정지된 사용자 접근 시도: userId={}", userId);
					throw new ApiException(ErrorCode.FORBIDDEN_ACCOUNT_PERMANENTLY_SUSPENDED);
				}
			}
		}
	}

	/**
	 * 유저 정보를 받아서 시큐리티 컨텍스트 홀더에 저장하는 메서드
	 * @param userInfo : userId, userName, roleID를 담고있는 DTO
	 */
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
