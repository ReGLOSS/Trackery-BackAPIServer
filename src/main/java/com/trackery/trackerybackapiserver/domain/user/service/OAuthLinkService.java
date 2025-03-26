package com.trackery.trackerybackapiserver.domain.user.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : OAuthLinkService
 * author         : inari
 * date           : 25. 3. 24.
 * description    : OAuth 계정 연동을 위한 토큰 관리 서비스입니다.
 *					소셜 계정 연동을 위한 토큰을 생성, 검증, 삭제하는 기능을 제공합니다.
 *					Redis를 사용하여 토큰 정보를 임시 저장합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.        inari       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLinkService {

	// Redis 키 접두사 및 토큰 유효시간
	private static final String LINK_TOKEN_PREFIX = "oauth:link:";
	private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(10);
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 계정 연동을 위한 토큰을 생성합니다.
	 *
	 * @param provider 소셜 계정 제공자
	 * @param email 연동할 이메일 주소
	 * @return 생성된 연동 토큰 문자열
	 */
	public String createLinkToken(String provider, String email) {
		// 고유 토큰 생성
		String token = UUID.randomUUID().toString();
		String key = LINK_TOKEN_PREFIX + token;

		// 연동 정보 저장
		redisTemplate.opsForHash().put(key, "provider", provider);
		if (email != null) {
			redisTemplate.opsForHash().put(key, "email", email);
		}

		// 유효 시간 설정
		redisTemplate.expire(key, TOKEN_EXPIRY);

		log.info("계정 연동 토큰 생성: {}, provider: {}, email: {}", token, provider, email);
		return token;
	}

	/**
	 * 계정 연동 토큰의 유효성을 검증하고 연동 정보를 반환합니다.
	 *
	 * @param token 검증할 연동 토큰
	 * @return 토큰에 저장된 연동 정보를 담은 OAuthLinkRequest 객체
	 * @throws ApiException 토큰이 유효하지 않거나 필요한 정보가 없는 경우 발생
	 */
	public OAuthLinkRequestDto validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String key = LINK_TOKEN_PREFIX + token;

		// Redis에서 토큰 정보 조회
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
			log.error("유효하지 않은 연동 토큰: {}", token);
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}

		Map<Object, Object> linkInfo = redisTemplate.opsForHash().entries(key);

		String provider = (String) linkInfo.get("provider");
		String email = (String) linkInfo.get("email");

		if (provider == null) {
			log.error("연동 토큰에 provider 정보 없음: {}", token);
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		return OAuthLinkRequestDto.builder()
			.provider(provider)
			.email(email)
			.linkAccount(true)
			.build();
	}

	/**
	 * 연동 토큰을 삭제합니다.
	 *
	 * @param token 삭제할 연동 토큰
	 */
	public void deleteToken(String token) {
		if (token != null && !token.trim().isEmpty()) {
			String key = LINK_TOKEN_PREFIX + token;
			redisTemplate.delete(key);
			log.info("연동 토큰 삭제: {}", token);
		}
	}
}
