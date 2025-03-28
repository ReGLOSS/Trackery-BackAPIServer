
package com.trackery.trackerybackapiserver.domain.jwt.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.service
 * fileName       : JwtRedisServiceTest
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    : JwtRedisService 단위테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.        durururuk      최초 생성
 */
@ExtendWith(MockitoExtension.class)
class JwtRedisServiceTest {
	@InjectMocks
	private JwtRedisService jwtRedisService;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private ObjectMapper objectMapper;

	@Test
	void 리프레시토큰_저장_테스트_성공() throws JsonProcessingException {
		// given
		RefreshTokenDto refreshTokenDto = new RefreshTokenDto("refreshToken", "jid", "subject");
		String redisKey = "jwtRefreshToken:" + refreshTokenDto.refreshToken();
		String expectedJson = "refreshTokenDtoJson";

		when(objectMapper.writeValueAsString(refreshTokenDto)).thenReturn(expectedJson);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		doNothing().when(valueOperations).set(redisKey, expectedJson);

		jwtRedisService.saveRefreshToken(refreshTokenDto);

		verify(objectMapper, times(1)).writeValueAsString(refreshTokenDto);
		verify(redisTemplate.opsForValue(), times(1)).set(redisKey, expectedJson);
	}
}
