package com.trackery.trackerybackapiserver.domain.common.util;

import static com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : PasswordUtilTest
 author         : durururuk
 date           : 25. 2. 14.
 description    : PasswordUtil 테스트코드
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 14.        durururuk       최초 생성*/
class PasswordUtilTest {

	//salt를 생성하고 base64로 디코딩 했을 때 16바이트 수인지 확인
	@Test
	void salt_생성_테스트() {
		String saltString = generateSalt();
		byte[] decodedSalt = Base64.getDecoder().decode(saltString);
		assertEquals(16, decodedSalt.length);
	}

	// salt 10000번 생성했을 때 중복 없는지 확인
	@Test
	void salt_난수_테스트() {
		Set<String> saltSet = new HashSet<>();
		int testCount = 10000;
		boolean result = true;

		for (int i = 0; i < testCount; i++) {
			String salt = generateSalt();
			if (!saltSet.add(salt)) {
				result = false;
			}
		}

		assertTrue(result);
	}

	//같은 평문 비밀번호, 같은 salt로 해싱했을 때 값이 같은지 검증
	@Test
	void 해싱_값_검증() {
		String rawPassword = "qwerasdf1234!";
		String salt = generateSalt();

		String hashedPassword1 = PasswordUtil.hashPassword(rawPassword, salt);
		String hashedPassword2 = PasswordUtil.hashPassword(rawPassword, salt);

		assertEquals(hashedPassword1, hashedPassword2);
	}
}