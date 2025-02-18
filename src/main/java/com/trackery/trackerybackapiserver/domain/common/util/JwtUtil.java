package com.trackery.trackerybackapiserver.domain.common.util;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : JwtUtil
 author         : durururuk
 date           : 25. 2. 18.
 description    : JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 18.        durururuk       최초 생성*/

@Slf4j
@Component
public class JwtUtil {

	//만료시간 10분(600초)
	private static final long EXPIRATION_TIME = 600;
	private final Algorithm algorithm;

	public JwtUtil(@Value("${JWT_SECRET_KEY}") String jwtSecretKey) {
		this.algorithm = Algorithm.HMAC256(jwtSecretKey);
	}

	//TODO 유저 권한 추가 필요

	/**
	 * JWT 생성 메서드
	 *
	 * @param userId : 인증할 유저ID
	 * @param userName : 인증할 유저명
	 * @return : 생성된 JWT 토큰
	 */
	public String generateJwt(Long userId, String userName) {
		try {
			return JWT.create()
				.withIssuer("trackery.bokkurin.com")
				.withSubject(userId.toString())
				.withClaim("username", userName)
				.withNotBefore(Instant.now())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plusSeconds(EXPIRATION_TIME))
				.withJWTId(UUID.randomUUID().toString())
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}
	}
}
