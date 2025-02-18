package com.trackery.trackerybackapiserver.domain.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : PasswordUtil
 author         : durururuk
 date           : 25. 2. 14.
 description    : 비밀번호 생성에 필요한 salt 생성기와 해싱을 담당하는 유틸리티 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 14.        durururuk       최초 생성*/
public class PasswordUtil {

	private PasswordUtil() {
		throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_UTIL_CLASS_INSTANTIATED);
	}

	private static final int HASH_ITERATIONS = 10000;

	/**
	 * 16바이트 난수를 생성하고 BASE64로 인코딩한 salt 생성하는 메서드
	 *
	 * @return 생성된 salt
	 */
	public static String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	/**
	 * 비밀번호와 salt를 조합해 SHA-256으로 해싱하는 메서드
	 *
	 * @param password : 입력된 평문 비밀번호
	 * @param salt : 기존 생성된 salt
	 * @return : 해싱된 비밀번호
	 */
	public static String hashPassword(String password, String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String saltedPassword = password + salt;
			byte[] hashedPassword = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

			for (int i = 0; i < HASH_ITERATIONS; i++) {
				hashedPassword = md.digest(hashedPassword);
			}

			return Base64.getEncoder().encodeToString(hashedPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_NO_SUCH_ALGORITHM);
		}
	}
}
