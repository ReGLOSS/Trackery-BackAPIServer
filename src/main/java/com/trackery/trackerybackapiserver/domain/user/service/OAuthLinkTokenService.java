package com.trackery.trackerybackapiserver.domain.user.service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

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
 * 25. 3. 26.        inari       프로바이더 및 이메일을 필수사항으로 지정
 * 25. 3. 27.        inari       코드 스멜 수정
 * 25. 6. 24.        inari		 	linkToken을 이용하는 방식으로 변경 및 이름 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLinkTokenService {

	// Redis 키 접두사 및 토큰 유효시간
	private static final String LINK_TOKEN_PREFIX = "oauth:link:";
	private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(10);
	private final RedisTemplate<String, Object> redisTemplate;
	private final UserMapper userMapper;

	/**
	 * 계정 연동을 위한 토큰을 생성합니다. (이메일 기반 - 기존 API 호환성용)
	 *
	 * @param provider 소셜 계정 제공자
	 * @param email 연동할 이메일 주소
	 * @return 생성된 연동 토큰 문자열
	 */
	public String createLinkToken(String provider, String email) {
		// 이메일로 사용자 조회
		Optional<User> user = userMapper.findByEmail(email);
		if (user.isEmpty()) {
			throw new ApiException(ErrorCode.NOT_FOUND);
		}
		// userId 기반 메서드 호출
		return createLinkToken(provider, user.get().getUserId());
	}

	/**
	 * 계정 연동을 위한 토큰을 생성합니다. (userId 기반)
	 *
	 * @param provider 소셜 계정 제공자
	 * @param userId 연동할 사용자 ID
	 * @return 생성된 연동 토큰 문자열
	 */
	public String createLinkToken(String provider, Long userId) {

		// provider Optional로 변환 및 필수 검증
		Optional<String> providerValue = Optional.ofNullable(provider)
			.filter(e -> !e.trim().isEmpty());

		// 프로바이더 필수 확인
		if (providerValue.isEmpty()) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		}

		// userId 필수 확인
		if (userId == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_USER_AUTH);
		}

		// 고유 토큰 생성
		String token = UUID.randomUUID().toString();
		String key = LINK_TOKEN_PREFIX + token;

		// 연동 정보 저장
		redisTemplate.opsForHash().put(key, "provider", provider);
		redisTemplate.opsForHash().put(key, "userId", userId.toString());

		// 유효 시간 설정
		redisTemplate.expire(key, TOKEN_EXPIRY);

		log.info("계정 연동 토큰 생성: {}, provider: {}, userId: {}", token, provider, userId);
		return token;
	}

	/**
	 * 계정 연동 토큰의 유효성을 검증하고 연동 정보를 반환합니다.
	 *
	 * @param token 검증할 연동 토큰
	 * @return 토큰에 저장된 연동 정보를 담은 Long userId
	 * @throws ApiException 토큰이 유효하지 않거나 필요한 정보가 없는 경우 발생
	 */
	public Long validateToken(String token) {
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

		// Optional로 변환하여 처리
		Optional<String> provider = Optional.ofNullable((String) linkInfo.get("provider"));
		Optional<String> userIdStr = Optional.ofNullable((String) linkInfo.get("userId"));

		// provider 필수 검증
		String providerValue = provider.orElseThrow(() -> {
			log.error("연동 토큰에 provider 정보 없음: {}", token);
			return new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		});

		// userId 필수 검증 및 변환
		Long userId = userIdStr.map(Long::parseLong).orElseThrow(() -> {
			log.error("연동 토큰에 userId 정보 없음: {}", token);
			return new ApiException(ErrorCode.BAD_REQUEST);
		});

		log.info("토큰 검증 성공: provider={}, userId={}", providerValue, userId);
		return userId;
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
