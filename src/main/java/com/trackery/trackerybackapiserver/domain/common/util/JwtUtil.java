package com.trackery.trackerybackapiserver.domain.common.util;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
 25. 2. 18.        durururuk       최초 생성
 */
//TODO 레디스 설정 후 액세스 토큰 + 리프레쉬 토큰으로 변경 필요
@Slf4j
@Component
public class JwtUtil {

	//만료시간 10분(600초)
	private static final long EXPIRATION_TIME = 600;

	private final String projectDomain;
	private final Algorithm algorithm;

	public JwtUtil(@Value("${JWT_SECRET_KEY}") String jwtSecretKey, @Value("${PROJECT_DOMAIN}") String projectDomain) {
		this.algorithm = Algorithm.HMAC256(jwtSecretKey);
		this.projectDomain = projectDomain;
	}

	/**
	 * JWT 생성 메서드
	 *
	 * @param userId : 인증할 유저ID
	 * @param userName : 인증할 유저명
	 * @return : 생성된 JWT 토큰
	 */
	public String generateJwt(Long userId, String userName, Long roleId) {
		try {
			return JWT.create()
				.withIssuer(projectDomain)
				.withSubject(userId.toString())
				.withClaim("username", userName)
				.withClaim("role", roleId)
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

	//TODO 레디스 설정 후 JID, 추가 검증 필요
	/**
	 * JWT 파싱, 검증 해주는 메서드
	 * @param token : jwt 토큰
	 * @return : jwt의 디코딩된 정보를 담고있는 DecodedJWT 객체
	 */
	public DecodedJWT verifyJwt(String token) {
		try {
			JWTVerifier verifier = JWT.require(algorithm)
				.withIssuer(projectDomain)
				.build();

			return verifier.verify(token);
		} catch (JWTVerificationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.UNAUTHORIZED_JWT_VERIFY_FAILED);
		}
	}
}
