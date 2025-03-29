package com.trackery.trackerybackapiserver.domain.jwt.service;

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
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.RefreshTokenDto;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : JwtUtil
 * author         : durururuk
 * date           : 25. 2. 18.
 * description    : JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 18.       durururuk       최초 생성
 * 25. 3. 28.		durururuk		리프레시 토큰 생성 메서드 추가
 */
@Slf4j
@Component
public class JwtService {
	private final JwtRedisService jwtRedisService;

	//만료시간 10분(600초)
	//TODO JWT 만료시간 일괄 설정되게 수정
	private static final int ACCESS_TOKEN_EXPIRATION_TIME = 600;
	private static final int REFRESH_TOKEN_EXPIRATION_TIME = 604800;

	private final String projectDomain;
	private final Algorithm algorithm;

	public JwtService(JwtRedisService jwtRedisService,
		@Value("${JWT_SECRET_KEY}") String jwtSecretKey,
		@Value("${PROJECT_DOMAIN}") String projectDomain) {
		this.jwtRedisService = jwtRedisService;
		this.algorithm = Algorithm.HMAC256(jwtSecretKey);
		this.projectDomain = projectDomain;
	}

	/**
	 * 액세스 토큰 생성 메서드
	 *
	 * @param userId : 인증할 유저ID
	 * @param userName : 인증할 유저명
	 * @return : 생성된 JWT 토큰
	 */
	public String generateAccessToken(Long userId, String userName, Long roleId) {
		try {
			return JWT.create()
				.withIssuer(projectDomain)
				.withSubject(userId.toString())
				.withClaim("username", userName)
				.withClaim("role", roleId)
				.withNotBefore(Instant.now())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_TIME))
				.withJWTId(UUID.randomUUID().toString())
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}
	}

	/**
	 * 유저 ID로 리프레시 토큰 발급하는 메서드
	 *
	 * @param userId : 유저 ID
	 * @return : 리프레시 토큰
	 */
	public RefreshTokenDto generateRefreshToken(Long userId) {
		String jid = UUID.randomUUID().toString();
		String refreshToken;

		try {
			refreshToken = JWT.create()
				.withIssuer(projectDomain)
				.withJWTId(jid)
				.withSubject(userId.toString())
				.withNotBefore(Instant.now())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_TIME))
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}

		return new RefreshTokenDto(refreshToken, jid, userId.toString());
	}

	/**
	 * 유저 정보를 받아서 액세스 토큰과 리프레시 토큰을 리스트로 반환하는 메서드
	 *
	 * @param userId : 유저 ID
	 * @param userName : 유저명
	 * @param roleId : 역할 ID
	 * @return : 액세스 토큰, 리프레시 토큰이 담긴 리스트
	 */
	public AuthTokenDto generateAccessTokenAndRefreshToken(Long userId, String userName, Long roleId) {
		String accessToken = generateAccessToken(userId, userName, roleId);
		String refreshToken = generateRefreshTokenAndSaveToRedis(userId);

		return new AuthTokenDto(accessToken, refreshToken);
	}

	/**
	 * 리프레시 토큰을 발급하고 정보를 redis에 저장하고 토큰을 반환하는 메서드
	 *
	 * @param userId : 유저 ID
	 * @return 리프레시 토큰
	 */
	private String generateRefreshTokenAndSaveToRedis(Long userId) {
		RefreshTokenDto refreshTokenDto = generateRefreshToken(userId);
		jwtRedisService.saveRefreshToken(refreshTokenDto);
		return refreshTokenDto.refreshToken();
	}

	/**
	 * JWT 파싱, 검증 해주는 메서드
	 * @param token : jwt 토큰
	 * @return : jwt의 디코딩된 정보를 담고있는 DecodedJWT 객체
	 */
	public DecodedJWT verifyJwt(String token) throws JWTVerificationException {
		JWTVerifier verifier = JWT.require(algorithm)
			.withIssuer(projectDomain)
			.build();

		return verifier.verify(token);
	}

	/**
	 * 특정 Subject 가지는 JWT 토큰 생성 메서드
	 * @param subject : 설정할 Subject
	 * @return : jwt 토큰
	 */
	public String generateTokenWithSubject(String subject) {
		try {
			return JWT.create()
				.withIssuer(projectDomain)
				.withSubject(subject)
				.withNotBefore(Instant.now())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_TIME))
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}
	}
}
