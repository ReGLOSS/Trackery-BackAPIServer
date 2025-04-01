package com.trackery.trackerybackapiserver.domain.jwt.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;

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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRedisService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String REFRESH_TOKEN_REDIS_KEY = "jwtRefreshToken:";

	/**
	 * 리프레시 토큰 정보를 Redis에 저장합니다.
	 * 토큰을 key, dto를 json 형식으로 변환하여 value 저장합니다.
	 *
	 * @param refreshTokenDto 리프레시 토큰, JwtId, 유저ID를 가지고 있는 DTO
	 */
	public void saveRefreshToken(RefreshTokenDto refreshTokenDto) {
		try {
			String redisKey = REFRESH_TOKEN_REDIS_KEY + refreshTokenDto.refreshToken();
			String jsonRefreshTokenDto = objectMapper.writeValueAsString(refreshTokenDto);

			redisTemplate.opsForValue().set(redisKey, jsonRefreshTokenDto);
		} catch (JsonProcessingException e) {
			log.error("Json 파싱 중 에러 발생 : ", e);
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
		String redisKey = REFRESH_TOKEN_REDIS_KEY + refreshToken;
		String jsonRefreshTokenDto = redisTemplate.opsForValue().get(redisKey);
		try {
			return objectMapper.readValue(jsonRefreshTokenDto, RefreshTokenDto.class);
		} catch (JsonProcessingException e) {
			log.error("Json 매핑 중 에러 발생 : ", e);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 한 번 사용된 리프레시 토큰을 레디스에서 제거하는 메서드입니다.
	 * @param refreshToken : 사용된 리프레시 토큰
	 */
	//Todo 추후 unlink로 변경
	public void deleteRefreshToken(String refreshToken) {
		String redisKey = REFRESH_TOKEN_REDIS_KEY + refreshToken;
		redisTemplate.delete(redisKey);
	}

}
