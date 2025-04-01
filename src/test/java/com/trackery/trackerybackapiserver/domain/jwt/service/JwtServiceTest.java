package com.trackery.trackerybackapiserver.domain.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;

import lombok.extern.slf4j.Slf4j;

/**
 packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : JwtUtilTest
 author         : durururuk
 date           : 25. 2. 18.
 description    : JwtUtil 테스트코드
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 18.        durururuk        최초 생성
 25. 2. 20.		   durururuk        jwt 생성 및 검증 테스트 코드 추가
 25. 4. 01.        durururuk		parseAndVerifyRefreshToken 테스트 코드 작성
 25. 4. 01.        durururuk		JWT 검증 실패 시 예외 에러 메시지 수정
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
	private JwtService jwtService;

	@Mock
	private JwtRedisService jwtRedisService;

	//운영환경에서 쓰이지 않는 테스트용 시크릿키입니다.
	final String jwtSecretKeyForTest = "and0c2VjcmV0a2V5Zm9ydGVzdA==";
	final String fakeProjectDomain = "www.a.com";

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(jwtRedisService, jwtSecretKeyForTest, fakeProjectDomain);
		jwtService = Mockito.spy(jwtService);
	}

	@Test
	void JWT_액세스_토큰_생성_검증_테스트() {
		String token = jwtService.generateAccessToken(1L, "abcdefg", 1L);

		DecodedJWT decodedJwt = jwtService.verifyJwt(token);

		assertEquals(1L, Long.valueOf(decodedJwt.getSubject()));
		assertEquals("abcdefg", decodedJwt.getClaim("username").asString());
		assertEquals(1L, decodedJwt.getClaim("role").asLong());
	}

	@Nested
	@DisplayName("JWT 검증")
	class verifyJwtTest {
		@Test
		@DisplayName("성공")
		void success() {
			String token = jwtService.generateAccessToken(1L, "abcdefg", 1L);

			DecodedJWT decodedJWT = jwtService.verifyJwt(token);

			assertNotNull(decodedJWT);
			assertEquals("1", decodedJWT.getSubject());
			assertEquals("abcdefg", decodedJWT.getClaim("username").asString());
			assertEquals(1L, decodedJWT.getClaim("role").asLong());
		}

		@Test
		@DisplayName("실패 - 유효하지 않은 JWT")
		void failure_1() {
			String token = "ㅁㄴㅇㄹ";

			ApiException exception = assertThrows(ApiException.class, () -> jwtService.verifyJwt(token));

			assertNotNull(exception);
			assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
		}
	}

	@Test
	void JWT_리프레시_토큰_생성_검증_테스트_성공() {
		doNothing().when(jwtRedisService).saveRefreshToken(any(RefreshTokenDto.class));

		RefreshTokenDto refreshTokenDto = jwtService.generateRefreshToken(1L);

		DecodedJWT decodedJWT = jwtService.verifyJwt(refreshTokenDto.refreshToken());

		assertEquals(fakeProjectDomain, decodedJWT.getIssuer());
		assertEquals(1L, Long.valueOf(decodedJWT.getSubject()));
	}

	@Test
	void JWT_이메일_토큰_생성_테스트() {
		String email = "a@a.com";
		String token = jwtService.generateTokenWithSubject(email);

		DecodedJWT decodedJWT = jwtService.verifyJwt(token);

		assertEquals(fakeProjectDomain, decodedJWT.getIssuer());
		assertEquals(email, decodedJWT.getSubject());
	}

	@Nested
	@DisplayName("인증 토큰 DTO 변환")
	class generateAccessTokenAndRefreshTokenTest {
		private Long userId;
		private String userName;
		private Long roleId;

		@BeforeEach
		void setUp() {
			userId = 1L;
			userName = "abcdefg";
			roleId = 1L;
		}

		@Test
		@DisplayName("성공")
		void success() {
			doNothing().when(jwtRedisService).saveRefreshToken(any(RefreshTokenDto.class));

			AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(userId, userName, roleId);

			DecodedJWT decodedAccessToken = jwtService.verifyJwt(authTokenDto.accessToken());
			DecodedJWT decodedRefreshToken = jwtService.verifyJwt(authTokenDto.refreshToken());

			assertNotNull(authTokenDto.accessToken());
			assertNotNull(authTokenDto.refreshToken());
			assertEquals("1", decodedAccessToken.getSubject());
			assertEquals("abcdefg", decodedAccessToken.getClaim("username").asString());
			assertEquals(1L, decodedAccessToken.getClaim("role").asLong());
			assertEquals(fakeProjectDomain, decodedRefreshToken.getIssuer());
		}
	}

	@Nested
	@DisplayName("리프레시 토큰 파싱 및 검증 테스트")
	class parseAndVerifyRefreshTokenTests {
		DecodedJWT decodedJWT = mock(DecodedJWT.class);

		private final String VALID_TOKEN = "validRefreshToken";
		private final String INVALID_TOKEN = "invalidRefreshToken";

		private final String SUBJECT = "1";

		@Test
		@DisplayName("성공")
		void success() {
			String VALID_JID = "validJid";
			RefreshTokenDto refreshTokenDto = new RefreshTokenDto(VALID_TOKEN, VALID_JID, SUBJECT);

			doReturn(decodedJWT).when(jwtService).verifyJwt(VALID_TOKEN);
			when(jwtRedisService.getRefreshTokenInfo(VALID_TOKEN)).thenReturn(refreshTokenDto);
			when(decodedJWT.getId()).thenReturn(VALID_JID);

			Long userId = jwtService.parseAndVerifyRefreshToken(VALID_TOKEN);

			assertEquals(1L, userId);
			verify(jwtRedisService).deleteRefreshToken(VALID_TOKEN);
		}

		@Test
		@DisplayName("실패 - 유효하지 않은 리프레시 토큰")
		void shouldThrowUnauthorizedExceptionForInvalidRefreshToken() {
			doThrow(new ApiException(ErrorCode.BAD_REQUEST)).when(jwtService).verifyJwt(INVALID_TOKEN);

			ApiException exception = assertThrows(ApiException.class,
				() -> jwtService.parseAndVerifyRefreshToken(INVALID_TOKEN));

			assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
			verify(jwtRedisService, never()).deleteRefreshToken(INVALID_TOKEN);
		}

		@Test
		@DisplayName("실패 - 유효하지 않은 JID")
		void shouldThrowUnauthorizedExceptionForInvalidJid() {
			String INVALID_JID = "invalidJid";
			RefreshTokenDto refreshTokenDto = new RefreshTokenDto(VALID_TOKEN, INVALID_JID, SUBJECT);

			doReturn(decodedJWT).when(jwtService).verifyJwt(VALID_TOKEN);
			when(jwtRedisService.getRefreshTokenInfo(VALID_TOKEN)).thenReturn(refreshTokenDto);
			when(decodedJWT.getId()).thenReturn("differentJid");

			ApiException exception = assertThrows(ApiException.class,
				() -> jwtService.parseAndVerifyRefreshToken(VALID_TOKEN));

			assertEquals(ErrorCode.UNAUTHORIZED_JWT_VERIFY_FAILED, exception.getErrorCode());
			verify(jwtRedisService, never()).deleteRefreshToken(VALID_TOKEN);
		}
	}

}
