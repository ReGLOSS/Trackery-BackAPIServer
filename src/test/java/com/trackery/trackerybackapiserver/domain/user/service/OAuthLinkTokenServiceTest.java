package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : OAuthLinkTokenServiceTest
 * author         : inari
 * date           : 25. 3. 26.
 * description    : OAuthLinkServiceTest 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.		inari		최초 생성
 * 25. 3. 26.		inari		연동유무를 세션에서 토큰으로 이전
 * 25. 3. 26.		inari		토큰 생성시 검증부분 테스트 추가
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 6. 24.		inari		기능 개선 및 테스트코드 수정 및 추가
 * 25. 6. 24.		inari		OAuthLinkTokenService로 OAuthLinkService 변경
 */
@ExtendWith(MockitoExtension.class)
class OAuthLinkTokenServiceTest {

	private static final String LINK_TOKEN_PREFIX = "oauth:link:";
	private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(10);
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private HashOperations<String, Object, Object> hashOperations;
	@Mock
	private UserMapper userMapper;
	@InjectMocks
	private OAuthLinkTokenService oAuthLinkTokenService;

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
		User user = User.builder().email(email).build();
		ReflectionTestUtils.setField(user, "userId", 1L);
		when(userMapper.findByEmail(email)).thenReturn(Optional.of(user));

		// when
		String token = oAuthLinkTokenService.createLinkToken(provider, email);

		// then
		assertNotNull(token);
		verify(userMapper).findByEmail(email);
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("provider"), eq(provider));
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("userId"), eq("1"));
		verify(redisTemplate).expire(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq(TOKEN_EXPIRY));
	}

	@Test
	@DisplayName("토큰 생성 - 프로바이더 null")
	void createLinkToken_NullProvider_ThrowsException() {
		// given
		String provider = null;
		String email = "test@example.com";
		User user = User.builder().email(email).build();
		ReflectionTestUtils.setField(user, "userId", 1L);
		when(userMapper.findByEmail(email)).thenReturn(Optional.of(user));

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 프로바이더 빈 문자열")
	void createLinkToken_EmptyProvider_ThrowsException() {
		// given
		String provider = "  ";
		String email = "test@example.com";
		User user = User.builder().email(email).build();
		ReflectionTestUtils.setField(user, "userId", 1L);
		when(userMapper.findByEmail(email)).thenReturn(Optional.of(user));

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 이메일 null")
	void createLinkToken_NullEmail_ThrowsException() {
		// given
		String provider = "google";
		String email = null;
		when(userMapper.findByEmail(email)).thenReturn(Optional.empty());

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - 이메일 빈 문자열")
	void createLinkToken_EmptyEmail_ThrowsException() {
		// given
		String provider = "naver";
		String email = "";
		when(userMapper.findByEmail(email)).thenReturn(Optional.empty());

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.createLinkToken(provider, email);
		});
		assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 생성 - userId 기반 성공")
	void createLinkToken_WithUserId_Success() {
		// given
		String provider = "KAKAO";
		Long userId = 1L;

		// when
		String token = oAuthLinkTokenService.createLinkToken(provider, userId);

		// then
		assertNotNull(token);
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("provider"), eq(provider));
		verify(hashOperations).put(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq("userId"), eq("1"));
		verify(redisTemplate).expire(argThat(s -> s.startsWith(LINK_TOKEN_PREFIX)), eq(TOKEN_EXPIRY));
	}

	@Test
	@DisplayName("토큰 생성 - userId null 예외")
	void createLinkToken_NullUserId_ThrowsException() {
		// given
		String provider = "KAKAO";
		Long userId = null;

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.createLinkToken(provider, userId);
		});
		assertEquals(ErrorCode.BAD_REQUEST_INVALID_USER_AUTH, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - 유효한 토큰")
	void validateToken_ValidToken_Success() {
		// given
		String token = "valid-token";
		String provider = "naver";
		Long userId = 1L;
		String key = LINK_TOKEN_PREFIX + token;

		Map<Object, Object> linkInfo = new HashMap<>();
		linkInfo.put("provider", provider);
		linkInfo.put("userId", userId.toString());

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		// when
		Long result = oAuthLinkTokenService.validateToken(token);

		// then
		assertNotNull(result);
		assertEquals(userId, result);
	}

	@Test
	@DisplayName("토큰 검증 - 유효한 토큰, userId 없음")
	void validateToken_ValidToken_NoUserId_ThrowsException() {
		// given
		String token = "valid-token-no-userid";
		String provider = "apple";
		String key = LINK_TOKEN_PREFIX + token;

		Map<Object, Object> linkInfo = new HashMap<>();
		linkInfo.put("provider", provider);
		// userId 없음

		when(redisTemplate.hasKey(key)).thenReturn(true);
		when(hashOperations.entries(key)).thenReturn(linkInfo);

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.validateToken(token);
		});
		assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 - 빈 토큰")
	void validateToken_EmptyToken_ThrowsException() {
		// given
		String token = "";

		// when & then
		ApiException exception = assertThrows(ApiException.class, () -> {
			oAuthLinkTokenService.validateToken(token);
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
			oAuthLinkTokenService.validateToken(token);
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
			oAuthLinkTokenService.validateToken(token);
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
			oAuthLinkTokenService.validateToken(token);
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
		oAuthLinkTokenService.deleteToken(token);

		// then
		verify(redisTemplate).delete(key);
	}

	@Test
	@DisplayName("토큰 삭제 - 빈 토큰")
	void deleteToken_EmptyToken_DoesNothing() {
		// given
		String token = "";

		// when
		oAuthLinkTokenService.deleteToken(token);

		// then
		verify(redisTemplate, never()).delete(any(String.class));
	}

	@Test
	@DisplayName("토큰 삭제 - null 토큰")
	void deleteToken_NullToken_DoesNothing() {
		// given
		String token = null;

		// when
		oAuthLinkTokenService.deleteToken(token);

		// then
		verify(redisTemplate, never()).delete(any(String.class));
	}
}
