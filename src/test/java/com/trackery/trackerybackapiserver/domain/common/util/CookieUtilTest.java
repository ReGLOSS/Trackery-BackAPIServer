package com.trackery.trackerybackapiserver.domain.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.enums.SameSitePolicy;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : CookieUtilTest
 * author         : durururuk
 * date           : 25. 2. 26.
 * description    : CookieUtil 단위테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.		durururuk		최초 생성
 * 25. 2. 26.		durururuk		액세스토큰 쿠키 생성 성공 케이스 단위테스트 작성
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 * 25. 3. 14.		durururuk		테스트 코드 작성
 * 25. 3. 28.		durururuk		refresh-token/ Cookie 유효시간 수정
 * 25. 4. 1.		durururuk		refresh-token/ extractCookieValue 테스트 코드 작성
 * 25. 6. 27.		Nari-Lee		쿠키 이름 및 정책 enum으로 수정
 */
class CookieUtilTest {

	@Test
	void 액세스_토큰_쿠키_생성_성공() {
		String jwt = "jwt";

		ResponseCookie cookie = CookieUtil.createHttpOnlyCookie(CookieName.ACCESS_TOKEN.getValue() ,jwt, Duration.ofMinutes(60));

		assertNotNull(cookie);
		assertEquals(CookieName.ACCESS_TOKEN.getValue(), cookie.getName());
		assertEquals(jwt, cookie.getValue());
		assertTrue(cookie.isHttpOnly());
		assertEquals(SameSitePolicy.STRICT.getValue(), cookie.getSameSite());
		assertEquals("/", cookie.getPath());
	}

	@Nested
	@DisplayName("쿠키 추출 테스트")
	class extractCookieValueTest{
		HttpServletRequest request;

		@BeforeEach
		void setUp() {
			request = mock(HttpServletRequest.class);
		}

		@Test
		@DisplayName("성공 - 쿠키 추출 성공")
		void success() {
			Cookie cookie = new Cookie("testCookie", "testValue");
			when(request.getCookies()).thenReturn(new Cookie[] {cookie});

			String result = CookieUtil.extractCookieValue(request, "testCookie", () -> "default");

			assertEquals("testValue", result);
		}

		@Test
		@DisplayName("성공 - 대체 값 반환 성공")
		void success_missingCookie() {
			when(request.getCookies()).thenReturn(null);

			String result = CookieUtil.extractCookieValue(request, "missingCookie", () -> "default");

			assertEquals("default", result);
		}
	}
}
