package com.trackery.trackerybackapiserver.domain.user.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.client.OAuthClient;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;
import com.trackery.trackerybackapiserver.domain.user.mapper.OAuthMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : OAuthService
 * author         : inari
 * date           : 25. 2. 26.
 * description    : 간편 로그인 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.        inari       최초 생성
 * 25. 2. 27.        inari       userRoleMapper 추가
 * 25. 2. 28.        inari       리프레시 토큰 제거
 * 25. 3. 14.        inari       주석 추가
 * 25. 3. 27.        inari		 provider를 enum으로 변경
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

	private final UserMapper userMapper;
	private final OAuthMapper oAuthMapper;
	private final JwtService jwtService;
	private final UserRoleMapper userRoleMapper;
	private final OAuthClient oAuthClient;
	private final SecureRandom random = new SecureRandom();

	/**
	 * OAuth 로그인 처리 매서드입니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @return OAuth 로그인 결과와 JWT 토큰
	 */
	@Transactional
	public OAuthLoginResult processOAuthLogin(OAuthLoginDto oAuthLoginDto) {

		OAuthProvider provider = OAuthProvider.valueOf(oAuthLoginDto.getProvider());

		// 인증 코드로 토큰 획득
		String accessToken = oAuthClient.getAccessToken(oAuthLoginDto.getCode(), provider);

		// 액세스 토큰으로 사용자 정보 획득
		OAuthUserInfoDto userInfo = oAuthClient.getUserInfo(accessToken, provider);

		// OAuth 연동 정보 조회
		Optional<OAuth> existingOAuth = oAuthMapper.findByProviderAndProviderId(provider.name(),
			userInfo.getProviderUserId());

		// 이미 OAuth 연동된 계정이 있으면 그대로 로그인
		if (existingOAuth.isPresent()) {
			// 기존 사용자로 JWT 토큰 생성
			User user = userMapper.findByUserId(existingOAuth.get().getUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

			UserRole userRole = userRoleMapper.findByUserId(user.getUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

			String jwt = jwtService.generateAccessToken(user.getUserId(), user.getUserName(), userRole.getRoleId());

			return new OAuthLoginResult(
				OAuthResponseDto.builder().isExistingEmail(false).build(),
				jwt
			);
		}

		// 동일한 이메일의 기존 계정이 있는지 확인
		if (userInfo.getEmail() != null) {
			Optional<User> existingUser = userMapper.findByEmail(userInfo.getEmail());

			if (existingUser.isPresent()) {
				// 연동을 원하지 않는 경우
				if (!oAuthLoginDto.isLinkAccount()) {
					return new OAuthLoginResult(
						OAuthResponseDto.builder()
							.isExistingEmail(true)
							.email(userInfo.getEmail())
							.build(),
						null
					);
				}

				// 연동을 원하는 경우 기존 계정과 연동
				linkOAuthToExistingUser(existingUser.get().getUserId(), userInfo);

				UserRole userRole = userRoleMapper.findByUserId(existingUser.get().getUserId())
					.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

				String jwt = jwtService.generateAccessToken(
					existingUser.get().getUserId(),
					existingUser.get().getUserName(),
					userRole.getRoleId()
				);

				return new OAuthLoginResult(
					OAuthResponseDto.builder().isExistingEmail(false).build(),
					jwt
				);
			}
		}

		// 신규 회원가입 진행
		User newUser = registerNewUser(userInfo);
		linkOAuthToExistingUser(newUser.getUserId(), userInfo);

		UserRole userRole = userRoleMapper.findByUserId(newUser.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

		String jwt = jwtService.generateAccessToken(newUser.getUserId(), newUser.getUserName(), userRole.getRoleId());

		return new OAuthLoginResult(
			OAuthResponseDto.builder().isExistingEmail(false).build(),
			jwt
		);
	}

	/**
	 * 신규 사용자 회원가입 매서드입니다.
	 *
	 * @param userInfo OAuth  사용자 정보
	 * @return 등록된 사용자 정보
	 */
	private User registerNewUser(OAuthUserInfoDto userInfo) {

		// 랜덤 비밀번호 생성
		String randomPassword = UUID.randomUUID().toString();
		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(randomPassword, salt);

		// 랜덤 유저명 생성
		String userName = generateUniqueUserName(userInfo.getNickname());

		// 사용자 생성
		User newUser = User.builder()
			.email(userInfo.getEmail())
			.userName(userName)
			.nickname(userInfo.getNickname() != null ? userInfo.getNickname() : userName)
			.password(hashedPassword)
			.salt(salt)
			.startDate(LocalDateTime.now())
			.status(1)
			.lastLogin(LocalDateTime.now())
			.userProfile(null)
			.build();

		userMapper.insertUser(newUser);

		// 기본 사용자 권한 부여
		UserRole userRole = UserRole.builder()
			.userId(newUser.getUserId())
			.roleId(1L)
			.build();

		userRoleMapper.insertUserRole(userRole);

		return newUser;
	}

	/**
	 * OAuth 정보를 기존 사용자와 연결하는 매서드입니다.
	 *
	 * @param userId 사용자 ID
	 * @param userInfo OAuth 사용자 정보
	 */
	private void linkOAuthToExistingUser(Long userId, OAuthUserInfoDto userInfo) {
		OAuth oAuth = OAuth.builder()
			.userId(userId)
			.provider(userInfo.getProvider())
			.providerUserId(userInfo.getProviderUserId())
			.build();

		oAuthMapper.insertOAuth(oAuth);
	}

	/**
	 * 사용자명 생성 매서드입니다.
	 *
	 * @param nickname 사용자 닉네임
	 * @return 유니크한 사용자명
	 */
	private String generateUniqueUserName(String nickname) {
		if (nickname == null || nickname.isEmpty()) {
			nickname = "user";
		}

		// 닉네임에서 공백 및 특수문자 제거
		String baseUserName = nickname.replaceAll("[^\\w]", "").replaceAll("\\s", "");
		if (baseUserName.length() > 10) {
			baseUserName = baseUserName.substring(0, 10);
		}

		// 영문, 숫자, 밑줄만 허용하는 정규식에 맞도록 조정
		if (!baseUserName.matches("^\\w+$")) {
			baseUserName = "user_";
		}

		// 랜덤 4자리 숫자 추가 (1000-9999)
		String userName = baseUserName + (1000 + random.nextInt(9000));

		// 사용자명 중복 체크 및 재생성
		int attempts = 0;
		while (userMapper.isExistsUserName(userName) && attempts < 10) {
			userName = baseUserName + (1000 + random.nextInt(9000));
			attempts++;
		}

		return userName;
	}

	/**
	 * OAuth 로그인 처리 결과 및 JWT 토큰을 담는 클래스
	 */
	@Getter
	@AllArgsConstructor
	public static class OAuthLoginResult {
		private OAuthResponseDto responseDto;
		private String jwtToken;
	}
}
