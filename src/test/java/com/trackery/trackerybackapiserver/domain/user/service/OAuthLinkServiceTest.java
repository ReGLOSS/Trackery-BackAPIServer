package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;


/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : OAuthLinkServiceTest
 * author         : inari
 * date           : 25. 3. 26.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.        inari       최초 생성
 * 25. 3. 26.        inari       프로바이더, 이메일 필수 사항으로 전환
 */
@ExtendWith(MockitoExtension.class)
class OAuthLinkServiceTest {

	private static final String LINK_TOKEN_PREFIX = "oauth:link:";
	private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(10);
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private HashOperations<String, Object, Object> hashOperations;
	@InjectMocks
	private OAuthLinkService oAuthLinkService;

	@BeforeEach
	void setUp() {
		// Mockito를 lenient 모드로 설정합니다.
		// 이는 사용되지 않는 스텁으로 인한 경고를 방지합니다.
		lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
	}

	@Test
	@DisplayName("토큰 생성 - 이메일 있는 경우")
	void createLinkToken_WithEmail_Success() {
		// given
		String provider = "kakao";
		String email = "test@example.com";

		// when
		String token = oAuthLinkService.createLinkToken(provider, email);

		// then
		assertNotNull(token);
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("provider"), eq(provider));
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("email"), eq(email));
		verify(redisTemplate).expire(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq(TOKEN_EXPIRY));
	}

	@Test
	@DisplayName("토큰 생성 - 프로바이더 null")
	void createLinkToken_NullProvider_ThrowsException() {
		// given
		String provider = null;
		String email = "test@example.com";

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 프로바이더 빈 문자열")
	void createLinkToken_EmptyProvider_ThrowsException() {
		// given
		String provider = "  ";
		String email = "test@example.com";

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 이메일 null")
	void createLinkToken_NullEmail_ThrowsException() {
		// given
		String provider = "google";
		String email = null;

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_INPUT_MISSING_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 이메일 빈 문자열")
	void createLinkToken_EmptyEmail_ThrowsException() {
		// given
		String provider = "naver";
		String email = "";

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_INPUT_MISSING_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - 유효한 토큰")
	void validateToken_ValidToken_Success() {
		// given
		String token = "valid-token";
		String provider = "naver";
		String email = "user@example.com";
		String key = LINK_TOKEN_PREFIX + token;

		Map<Object, Object> linkInfo = new HashMap<>();
		linkInfo.put("provider", provider);
		linkInfo.put("email", email);

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		// when
		OAuthLinkRequestDto result = oAuthLinkService.validateToken(token);

		// then
		assertNotNull(result);
		assertEquals(provider, result.getProvider());
		assertEquals(email, result.getEmail());
		assertTrue(result.isLinkAccount());
	}

	@Test
	@DisplayName("토큰 검증 - 유효한 토큰, 이메일 없음")
	void validateToken_ValidToken_NoEmail_Success() {
		// given
		String token = "valid-token-no-email";
		String provider = "apple";
		String key = LINK_TOKEN_PREFIX + token;

		Map<Object, Object> linkInfo = new HashMap<>();
		linkInfo.put("provider", provider);
		// 이메일 없음

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.validateToken(token);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_INPUT_MISSING_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - 빈 토큰")
	void validateToken_EmptyToken_ThrowsException() {
		// given
		String token = "";

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.validateToken(token);
		});
		assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - null 토큰")
	void validateToken_NullToken_ThrowsException() {
		// given
		String token = null;

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.validateToken(token);
		});
		assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - 존재하지 않는 토큰")
	void validateToken_NonExistentToken_ThrowsException() {
		// given
		String token = "non-existent-token";
		String key = LINK_TOKEN_PREFIX + token;

		when(redisTemplate.hasKey(key)).thenReturn(false);

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.validateToken(token);
		});
		assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - provider 없는 토큰")
	void validateToken_NoProvider_ThrowsException() {
		// given
		String token = "invalid-token";
		String key = LINK_TOKEN_PREFIX + token;
		String email = "test@example.com";

		Map<Object, Object> linkInfo = new HashMap<>();
		linkInfo.put("email", email);
		// provider가 없는 경우

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkService.validateToken(token);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 삭제 - 유효한 토큰")
	void deleteToken_ValidToken_Success() {
		// given
		String token = "token-to-delete";
		String key = LINK_TOKEN_PREFIX + token;

		// when
		oAuthLinkService.deleteToken(token);

		// then
		verify(redisTemplate).delete(key);
	}

	@Test
	@DisplayName("토큰 삭제 - 빈 토큰")
	void deleteToken_EmptyToken_DoesNothing() {
		// given
		String token = "";

		// when
		oAuthLinkService.deleteToken(token);

		// then
		verify(redisTemplate, never()).delete(any(String.class));
	}

	@Test
	@DisplayName("토큰 삭제 - null 토큰")
	void deleteToken_NullToken_DoesNothing() {
		// given
		String token = null;

		// when
		oAuthLinkService.deleteToken(token);

		// then
		verify(redisTemplate, never()).delete(any(String.class));
	}
}
