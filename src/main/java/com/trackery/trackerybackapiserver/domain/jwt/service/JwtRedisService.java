package com.trackery.trackerybackapiserver.domain.jwt.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.enums.JwtExpirationTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.service
 * fileName       : JwtRedisService
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    : Jwt 관련 Redis 작업을 하는 서비스 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.       durururuk       최초 생성
 * 25. 3. 28.		durururuk		리프레시 토큰 저장, 조회, 삭제 기능 구현
 * 25. 6. 25.		inari			액세스 토큰 블랙리스트 기능 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRedisService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String REFRESH_TOKEN_KEY_PREFIX = "jwtRefreshToken:";
	private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "jwtBlacklist:";

	/**
	 * 리프레시 토큰 정보를 Redis에 저장합니다.
	 * 토큰을 key, dto를 json 형식으로 변환하여 value 저장합니다.
	 *
	 * @param refreshTokenDto 리프레시 토큰, JwtId, 유저ID를 가지고 있는 DTO
	 */
	public void saveRefreshToken(RefreshTokenDto refreshTokenDto) {
		if (refreshTokenDto == null || refreshTokenDto.refreshToken() == null) {
			log.error("RefreshTokenDto 혹은 dto.refreshToken null");
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		try {
			String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshTokenDto.refreshToken();
			String jsonRefreshTokenDto = objectMapper.writeValueAsString(refreshTokenDto);

			Duration ttl = Duration.ofSeconds(JwtExpirationTime.REFRESH_TOKEN.getExpirationTime());
			redisTemplate.opsForValue().set(redisKey, jsonRefreshTokenDto, ttl);

			String savedValue = redisTemplate.opsForValue().get(redisKey);
			if (savedValue == null) {
				log.error("리프레시 토큰 저장 실패. Key: {}", redisKey);
				throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
			}

		} catch (JsonProcessingException e) {
			log.error("리프레시 토큰 Redis에 저장 중 직렬화 오류 발생", e);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Redis에 접근해서 리프레시 토큰을 가져오는 메서드입니다.
	 *
	 * @param refreshToken : 클라이언트한테 받아온 리프레시 토큰
	 * @return : redis에 저장된 리프레시 토큰 정보 (리프레시 토큰 자체를 파싱한 것이 아닙니다.)
	 */
	public RefreshTokenDto getRefreshTokenInfo(String refreshToken) {
		if (refreshToken == null || refreshToken.trim().isEmpty()) {
			log.warn("리프레시 토큰 비어있음");
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;

		try {
			boolean keyExists = redisTemplate.hasKey(redisKey);

			if (!keyExists) {
				log.warn("Redis에서 리프레시 토큰 조회 실패: {}", redisKey);
				throw new ApiException(ErrorCode.UNAUTHORIZED);
			}

			String jsonRefreshTokenDto = redisTemplate.opsForValue().get(redisKey);

			if (jsonRefreshTokenDto == null) {
				log.error("리프레시 토큰 Key는 있는데 값이 null, Key: {}", redisKey);
				redisTemplate.unlink(redisKey);
				throw new ApiException(ErrorCode.UNAUTHORIZED);
			}

			if (jsonRefreshTokenDto.contains("\0") || !isValidJson(jsonRefreshTokenDto)) {
				log.error("리프레시 토큰 깨짐. Key: {}, Data: {}",
					redisKey, jsonRefreshTokenDto.replaceAll("\\p{Cntrl}", "?"));
				redisTemplate.unlink(redisKey);
				throw new ApiException(ErrorCode.UNAUTHORIZED);
			}

			return objectMapper.readValue(jsonRefreshTokenDto, RefreshTokenDto.class);

		} catch (JsonProcessingException e) {
			log.error("리프레시 토큰 조회 후 역직렬화 중 에러 발생", e);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 한 번 사용된 리프레시 토큰을 레디스에서 제거하는 메서드입니다.
	 * @param refreshToken : 사용된 리프레시 토큰
	 */
	public void deleteRefreshToken(String refreshToken) {
		if (refreshToken == null || refreshToken.trim().isEmpty()) {
			log.warn("리프레시 토큰 레디스에서 삭제 실패 : key 없음");
			return;
		}

		String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;

		Boolean deleted = redisTemplate.delete(redisKey);
		log.info("Refresh token deletion result: {}", deleted);
	}

	/**
	 * 액세스 토큰을 블랙리스트에 추가하는 메서드입니다.
	 * @param jti JWT ID (토큰 고유 식별자)
	 * @param expirationTime 토큰 만료까지 남은 시간 (초)
	 */
	public void addAccessTokenToBlacklist(String jti, long expirationTime) {
		if (jti == null || jti.trim().isEmpty()) {
			log.error("JTI null이거나 비어있음");
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String redisKey = ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + jti;

		redisTemplate.opsForValue().set(redisKey, "blacklisted", Duration.ofSeconds(expirationTime));
	}

	/**
	 * 액세스 토큰이 블랙리스트에 있는지 확인하는 메서드입니다.
	 * @param jti JWT ID (토큰 고유 식별자)
	 * @return 블랙리스트에 있으면 true, 없으면 false
	 */
	public boolean isAccessTokenBlacklisted(String jti) {
		if (jti == null || jti.trim().isEmpty()) {
			return false;
		}

		String redisKey = ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + jti;

		return redisTemplate.hasKey(redisKey);
	}

	/**
	 * JSON 문자열 유효성 검사 헬퍼 메서드
	 */
	private boolean isValidJson(String jsonString) {
		try {
			objectMapper.readTree(jsonString);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
