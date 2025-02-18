package com.trackery.trackerybackapiserver.domain.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.algorithms.Algorithm;

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
 25. 2. 18.        durururuk       최초 생성*/
@Slf4j
class JwtUtilTest {
	private JwtUtil jwtUtil;

	//운영환경에서 쓰이지 않는 테스트용 시크릿키입니다.
	String jwtSecretKeyForTest = "and0c2VjcmV0a2V5Zm9ydGVzdA==";

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil(jwtSecretKeyForTest);
	}

	@Test
	void JWT_토큰_생성_테스트() {
		String jwt = jwtUtil.generateJwt(1L, "abcdefg");
		log.info(jwt);
		assertNotNull(jwt);
		assertTrue(jwt.startsWith("eyJ"));
	}
}
