package com.trackery.trackerybackapiserver.domain.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;

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
class JwtServiceTest {
	private JwtService jwtService;

	//운영환경에서 쓰이지 않는 테스트용 시크릿키입니다.
	final String jwtSecretKeyForTest = "and0c2VjcmV0a2V5Zm9ydGVzdA==";
	final String fakeProjectDomain = "www.a.com";

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(jwtSecretKeyForTest, fakeProjectDomain);
	}

	@Test
	void JWT_액세스_토큰_생성_검증_테스트() {
		String token = jwtService.generateAccessToken(1L, "abcdefg", 1L);

		DecodedJWT decodedJwt = jwtService.verifyJwt(token);

		assertEquals(1L, Long.valueOf(decodedJwt.getSubject()));
		assertEquals("abcdefg", decodedJwt.getClaim("username").asString());
		assertEquals(1L, decodedJwt.getClaim("role").asLong());
	}

	@Test
	void JWT_이메일_토큰_생성_테스트() {
		String email = "a@a.com";
		String token = jwtService.generateTokenWithSubject(email);

		DecodedJWT decodedJWT = jwtService.verifyJwt(token);

		assertEquals(fakeProjectDomain, decodedJWT.getIssuer());
		assertEquals(email, decodedJWT.getSubject());
	}
}
