
package com.trackery.trackerybackapiserver.domain.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.enums.JwtExpirationTime;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.service
 * fileName       : JwtRedisServiceTest
 * author         : durururuk
 * date           : 25. 3. 28.
 * description    : JwtRedisService 단위테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 28.       durururuk      최초 생성
 * 25. 3. 29.		durururuk	   테스트 코드 작성
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

	@Nested
	@DisplayName("리프레시 토큰 저장")
	class SaveRefreshTokenTest {
		private final RefreshTokenDto refreshTokenDto = new RefreshTokenDto("refreshToken", "jid", "subject");
		private final String redisKey = "jwtRefreshToken:" + refreshTokenDto.refreshToken();

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			String expectedJson = "refreshTokenDtoJson";
			when(objectMapper.writeValueAsString(refreshTokenDto)).thenReturn(expectedJson);
			doNothing().when(valueOperations)
				.set(redisKey, expectedJson, JwtExpirationTime.REFRESH_TOKEN.getExpirationTime());

			jwtRedisService.saveRefreshToken(refreshTokenDto);

			verify(objectMapper, times(1)).writeValueAsString(refreshTokenDto);
			verify(redisTemplate.opsForValue(), times(1)).set(redisKey, expectedJson,
				JwtExpirationTime.REFRESH_TOKEN.getExpirationTime());
		}

		@Test
		@DisplayName("실패 - json 파싱 에러")
		void failure_1() throws JsonProcessingException {
			when(objectMapper.writeValueAsString(refreshTokenDto)).thenThrow(JsonProcessingException.class);

			assertThrows(ApiException.class, () -> jwtRedisService.saveRefreshToken(refreshTokenDto));
		}

	}

	@Nested
	@DisplayName("리프레시 토큰 조회")
	class GetRefreshTokenTest {
		private final String refreshToken = "<PASSWORD>";
		private final String expectedJson = "refreshTokenDtoJson";
		private final RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refreshToken, "jid", "subject");

		@BeforeEach
		void setUp() {
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			String redisKey = "jwtRefreshToken:" + refreshToken;
			when(valueOperations.get(redisKey)).thenReturn(expectedJson);
		}

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			when(objectMapper.readValue(expectedJson, RefreshTokenDto.class)).thenReturn(refreshTokenDto);

			RefreshTokenDto result = jwtRedisService.getRefreshTokenInfo(refreshToken);

			assertEquals(refreshTokenDto, result);
		}

		@Test
		@DisplayName("실패 - Json 매핑 에러")
		void failure_1() throws JsonProcessingException {
			when(objectMapper.readValue(expectedJson, RefreshTokenDto.class)).thenThrow(JsonProcessingException.class);
			assertThrows(ApiException.class, () -> jwtRedisService.getRefreshTokenInfo(refreshToken));
		}
	}

	@Nested
	@DisplayName("리프레시 토큰 삭제")
	class DeleteRefreshTokenTest {

		@Test
		@DisplayName("성공")
		void success() {
			String refreshToken = "<PASSWORD>";
			String redisKey = "jwtRefreshToken:" + refreshToken;
			when(redisTemplate.delete(redisKey)).thenReturn(true);

			jwtRedisService.deleteRefreshToken(refreshToken);

			verify(redisTemplate, times(1)).delete(redisKey);
		}

	}
}
