package com.trackery.trackerybackapiserver.domain.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;

import lombok.extern.slf4j.Slf4j;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : JwtUtilTest
 author         : durururuk
 date           : 25. 2. 18.
 description    : JwtUtil 테스트코드
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 18.        durururuk       최초 생성
 25. 2. 20.		   durururuk       jwt 생성 및 검증 테스트 코드 추가
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


}
