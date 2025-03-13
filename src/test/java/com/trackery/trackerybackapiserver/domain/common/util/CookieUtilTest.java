package com.trackery.trackerybackapiserver.domain.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : CookieUtilTest
 * author         : durururuk
 * date           : 25. 2. 26.
 * description    : CookieUtil 단위테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.        durururuk      최초 생성
 * 25. 2. 26.        durururuk      액세스토큰 쿠키 생성 성공 케이스 단위테스트 작성
 */
class CookieUtilTest {

	@Test
	void 액세스_토큰_쿠키_생성_성공() {
		String jwt = "jwt";

		ResponseCookie cookie = CookieUtil.createHttpOnlyCookie(jwt);

		assertNotNull(cookie);
		assertEquals("accessToken", cookie.getName());
		assertEquals(jwt, cookie.getValue());
		assertTrue(cookie.isHttpOnly());
		assertEquals("Strict", cookie.getSameSite());
		assertEquals("/", cookie.getPath());
	}
}