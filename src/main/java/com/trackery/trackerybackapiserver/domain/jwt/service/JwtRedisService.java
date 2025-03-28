package com.trackery.trackerybackapiserver.domain.jwt.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
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
 * 25. 3. 28.        durururuk      최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRedisService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	public void saveRefreshToken(RefreshTokenDto refreshTokenDto) {
		try {
			String redisKey = "jwtRefreshToken:" + refreshTokenDto.refreshToken();
			String jsonRefreshTokenDto = objectMapper.writeValueAsString(refreshTokenDto);

			redisTemplate.opsForValue().set(redisKey, jsonRefreshTokenDto);
		} catch (JsonProcessingException e) {
			log.error("Json 파싱 중 에러 발생 : ", e);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public RefreshTokenDto getRefreshTokenInfo(String refreshToken) {
		String redisKey = "jwtRefreshToken:" + refreshToken;
		String jsonRefreshTokenDto = redisTemplate.opsForValue().get(redisKey);
		try {
			return objectMapper.readValue(jsonRefreshTokenDto, RefreshTokenDto.class);
		} catch (JsonProcessingException e) {
			log.error("Json 매핑 중 에러 발생 : ", e);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

}
