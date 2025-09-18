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
 * 25. 3. 28.		durururuk		최초 생성
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 28.		durururuk		레디스 키 상수화
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		주석 작성
 * 25. 3. 29.		durururuk		주석 작성
 * 25. 6. 19.		durururuk		redis에서 리프레시 토큰이 만료되어 key를 가져올 수 없을 때 예외 처리 추가
 * 25. 6. 19.		durururuk		Redis 데이터 삭제 시 처리 방식을 delete에서 비동기 방식인 unlink로 변경
 * 25. 6. 19.		durururuk		JWT 토큰 만료시간 enum으로 관리하게 수정
 * 25. 6. 25.		inari		jwt 액세스토큰 블랙리스트 추가
 * 25. 7. 9.		durururuk		리프레시 토큰으로 액세스토큰 재발급시 예외처리 강화 및 로깅 추가
 * 25. 7. 14.		durururuk		로그에서 민감정보 삭제
 * 25. 7. 14.		durururuk		테스트코드 수정
 * 25. 9. 18.		inari		유저 정지 기능 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRedisService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String REFRESH_TOKEN_KEY_PREFIX = "jwtRefreshToken:";
	private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "jwtBlacklist:";
	private static final String USER_SUSPENSION_KEY_PREFIX = "userSuspension:";

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

		Boolean deleted = redisTemplate.unlink(redisKey);
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
	 * 사용자를 정지 상태로 설정합니다.
	 * @param userId 정지할 사용자 ID
	 * @param suspensionType 정지 유형 (2: 임시정지, 3: 영구정지)
	 * @param endDate 정지 해제 예정일 (임시정지인 경우만, YYYY-MM-DD 형식)
	 */
	public void suspendUser(Long userId, Integer suspensionType, String endDate) {
		if (userId == null) {
			log.error("사용자 ID null");
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String redisKey = USER_SUSPENSION_KEY_PREFIX + userId;
		String suspensionInfo = suspensionType + ":" + (endDate != null ? endDate : "");

		// 영구정지는 10년, 임시정지는 필요에 따라 더 길게 설정 (최대 1년)
		Duration ttl = suspensionType == 3 ? Duration.ofDays(3650) : Duration.ofDays(365);

		redisTemplate.opsForValue().set(redisKey, suspensionInfo, ttl);
		log.info("사용자 정지 설정: userId={}, type={}, endDate={}", userId, suspensionType, endDate);
	}

	/**
	 * 사용자 정지를 해제합니다.
	 * @param userId 정지 해제할 사용자 ID
	 */
	public void unsuspendUser(Long userId) {
		if (userId == null) {
			log.error("사용자 ID null");
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		String redisKey = USER_SUSPENSION_KEY_PREFIX + userId;
		Boolean deleted = redisTemplate.unlink(redisKey);
		log.info("사용자 정지 해제: userId={}, deleted={}", userId, deleted);
	}

	/**
	 * 사용자가 정지 상태인지 확인합니다.
	 * @param userId 확인할 사용자 ID
	 * @return 정지 상태이면 true, 아니면 false
	 */
	public boolean isUserSuspended(Long userId) {
		if (userId == null) {
			return false;
		}

		String redisKey = USER_SUSPENSION_KEY_PREFIX + userId;
		return redisTemplate.hasKey(redisKey);
	}

	/**
	 * 사용자의 정지 정보를 가져옵니다.
	 * @param userId 사용자 ID
	 * @return 정지 정보 (형식: "suspensionType:endDate")
	 */
	public String getUserSuspensionInfo(Long userId) {
		if (userId == null) {
			return null;
		}

		String redisKey = USER_SUSPENSION_KEY_PREFIX + userId;
		return redisTemplate.opsForValue().get(redisKey);
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
