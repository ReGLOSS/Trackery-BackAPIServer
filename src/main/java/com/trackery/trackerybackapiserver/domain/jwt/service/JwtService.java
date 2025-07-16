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
import com.trackery.trackerybackapiserver.domain.jwt.enums.JwtExpirationTime;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.jwt.service
 * fileName       : JwtService
 * author         : durururuk
 * date           : 25. 2. 18.
 * description    : JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 18.		durururuk		최초 생성
 * 25. 2. 18.		durururuk		JWT 토큰 추가 메서드 추가
 * 25. 2. 19.		durururuk		Jwt 적용을 위한 필터 작성, 추가
 * 25. 2. 19.		durururuk		코딩 컨벤션에 맞게 정리
 * 25. 2. 19.		durururuk		Jwt 생성 시 역할 정보를 담도록 추가, jwt 필터에도 반영, 로직 개선
 * 25. 2. 20.		durururuk		회원가입 시 JWT를 담은 헤더를 같이 반환하도록 추가
 * 25. 2. 20.		durururuk		주석 추가
 * 25. 2. 24.		Nari-Lee		자바독 주석 추가
 * 25. 3. 4.		durururuk		이메일 인증 요청 기능 추가
 * 25. 3. 5.		durururuk		주석 추가
 * 25. 3. 14.		durururuk		예외처리 필터 작성
 * 25. 3. 14.		durururuk		예외처리 필터 작성
 * 25. 3. 14.		durururuk		유저명 사용가능할 시 userNameToken 쿠키에 추가
 * 25. 3. 27.		durururuk		액세스토큰쿠키 유지시간 jwt 만료시간과 같게 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 생성 메서드 추가
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		userService에 있던 토큰 관련 로직 JwtService로 이동
 * 25. 3. 28.		durururuk		주석 추가
 * 25. 3. 28.		durururuk		리프레시 토큰을 통한 액세스 토큰 재발급 기능 구현
 * 25. 3. 28.		durururuk		필터 순서 수정
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 3. 29.		durururuk		주석 작성
 * 25. 3. 29.		durururuk		JwtRedisService 테스트 코드 작성
 * 25. 3. 31.		Durururuk		JwtService 테스트코드 추가
 * 25. 4. 1.		Durururuk		JwtResolverFilter에 있던 분리된 메서드들 각자 있어야 할 클래스로 이동
 * 25. 4. 1.		durururuk		Bean 순환 문제 해결
 * 25. 4. 1.		durururuk		JWT 검증 실패 시 예외 에러메시지 수정
 * 25. 4. 8.		durururuk		액세스 토큰 10분 으로 돼있던 문제 수정
 * 25. 6. 19.		durururuk		JWT 토큰 만료시간 enum으로 관리하게 수정
 * 25. 6. 19.		durururuk		기존 액세스 토큰의 시간 1시간을 그대로 가져오던 이메일 인증 토큰, 유저명 중복 확인 토큰을 각각 처리하게 수정
 * 25. 6. 19.		durururuk		변경된 로직에 맞게 javaDoc 작성
 * 25. 6. 19.		durururuk		기존 JwtService에 선언돼있던 액세스토큰, 리프레시 토큰 만료시간 삭제
 * 25. 6. 25.		Nari-Lee		jwt 액세스토큰 블랙리스트 추가
 */
@Slf4j
@Component
public class JwtService {
	private final JwtRedisService jwtRedisService;

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
				.withExpiresAt(Instant.now().plusSeconds(JwtExpirationTime.ACCESS_TOKEN.getExpirationTime()))
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
				.withExpiresAt(Instant.now().plusSeconds(JwtExpirationTime.REFRESH_TOKEN.getExpirationTime()))
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}

		RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refreshToken, jid, userId.toString());

		jwtRedisService.saveRefreshToken(refreshTokenDto);

		return refreshTokenDto;
	}

	/**
	 * 유저 정보를 받아서 액세스 토큰과 리프레시 토큰을 DTO로 반환하는 메서드
	 *
	 * @param userId : 유저 ID
	 * @param userName : 유저명
	 * @param roleId : 역할 ID
	 * @return : 액세스 토큰, 리프레시 토큰이 담긴 DTO
	 */
	public AuthTokenDto generateAccessTokenAndRefreshToken(Long userId, String userName, Long roleId) {
		String accessToken = generateAccessToken(userId, userName, roleId);
		String refreshToken = generateRefreshToken(userId).refreshToken();

		return new AuthTokenDto(accessToken, refreshToken);
	}

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
			DecodedJWT jwt = verifier.verify(token);
			String jti = jwt.getId();
			if (jti != null && jwtRedisService.isAccessTokenBlacklisted(jti)) {
				throw new ApiException(ErrorCode.UNAUTHORIZED);
			}
			return jwt;

		} catch (JWTVerificationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}
	}

	/**
	 * 특정 Subject 가지는 JWT 토큰 생성 메서드
	 * @param subject : 설정할 Subject
	 * @param jwtExpirationTime : 만료시간 설정을 위한 JwtExpirationTime enum
	 * @return : jwt 토큰
	 */
	public String generateTokenWithSubject(String subject, JwtExpirationTime jwtExpirationTime) {
		try {
			return JWT.create()
				.withIssuer(projectDomain)
				.withSubject(subject)
				.withNotBefore(Instant.now())
				.withIssuedAt(Instant.now())
				.withExpiresAt(Instant.now().plusSeconds(jwtExpirationTime.getExpirationTime()))
				.sign(algorithm);
		} catch (JWTCreationException e) {
			log.error(e.getMessage());
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR_FAILED_TO_GENERATE_JWT);
		}
	}

	/**
	 * 리프레시 토큰을 파싱하고 검증한 뒤 유저 ID를 반환합니다.
	 * @param refreshToken : 리프레시 토큰
	 * @return : userId
	 */
	public Long parseAndVerifyRefreshToken(String refreshToken) {
		DecodedJWT decodedRefreshToken = verifyJwt(refreshToken);
		RefreshTokenDto refreshTokenDto = jwtRedisService.getRefreshTokenInfo(refreshToken);

		if (!decodedRefreshToken.getId().equals(refreshTokenDto.jid())) {
			throw new ApiException(ErrorCode.UNAUTHORIZED_JWT_VERIFY_FAILED);
		}

		jwtRedisService.deleteRefreshToken(refreshToken);

		return Long.valueOf(refreshTokenDto.subject());
	}
}
