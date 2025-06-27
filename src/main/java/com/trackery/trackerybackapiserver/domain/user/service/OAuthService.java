package com.trackery.trackerybackapiserver.domain.user.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackery.trackerybackapiserver.config.OAuthProperties;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.client.OAuthClient;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUrlResponseDto;
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
 * 25. 6. 23.        inari		 기존 유저에 간편 로그인 연동 추가
 * 25. 6. 24.        inari		 linkToken을 이용하는 방식으로 변경 및 안쓰는 코드 제거
 * 25. 6. 25.        inari		 리프레시 토큰 발급 추가
 * 25. 6. 26.        inari		 "state=" 상수화로 코드 스멜 제거
 * 25. 6. 27.		 inari	   	 로그인시 lastlogin 갱신 추가 및 이미 연동된 계정 타유저 접근 차단
 * 25. 6. 27.		 inari	   	 코드 복잡도 해결을 위해 메서드 분리
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
	private final OAuthProperties oAuthProperties;
	private final SecureRandom random = new SecureRandom();
	private static final String STATE = "state=";

	/**
	 * OAuth 로그인 처리 매서드입니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @return OAuth 로그인 결과와 JWT 토큰
	 */
	@Transactional
	public OAuthLoginResult processOAuthLogin(OAuthLoginDto oAuthLoginDto) {
		OAuthProvider provider = OAuthProvider.valueOf(oAuthLoginDto.getProvider());
		log.info("OAuth 로그인 처리 시작: provider={}, linkAccount={}, linkUserId={}",
			provider, oAuthLoginDto.isLinkAccount(), oAuthLoginDto.getLinkUserId());

		// 인증 코드로 토큰 획득 및 사용자 정보 획득
		OAuthUserInfoDto userInfo = getUserInfoFromOAuth(oAuthLoginDto.getCode(), provider);

		// 1단계: 기존 OAuth 연동 확인
		Optional<OAuth> existingOAuth = oAuthMapper.findByProviderAndProviderId(provider.name(),
			userInfo.getProviderUserId());

		if (existingOAuth.isPresent()) {
			return handleExistingOAuthLogin(oAuthLoginDto, existingOAuth.get());
		}

		// 1.5단계: link_token으로 직접 연동 처리
		if (oAuthLoginDto.isLinkAccount() && oAuthLoginDto.getLinkUserId() != null) {
			return handleLinkTokenBasedConnection(oAuthLoginDto, userInfo, provider);
		}

		// 2단계: 이메일로 기존 계정 확인
		if (userInfo.getEmail() != null && !userInfo.getEmail().trim().isEmpty()) {
			OAuthLoginResult emailResult = handleEmailBasedLogin(oAuthLoginDto, userInfo, provider);
			if (emailResult != null) {
				return emailResult;
			}
		}

		// 3단계: 연동 모드인데 연동할 기존 계정이 없는 경우 에러
		if (oAuthLoginDto.isLinkAccount()) {
			log.warn("연동 모드이지만 연동할 기존 계정을 찾을 수 없음");
			throw new ApiException(ErrorCode.NOT_FOUND);
		}

		// 4단계: 신규 회원가입 진행
		return handleNewUserRegistration(userInfo);
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
	 * link_token이 포함된 OAuth 인증 URL을 생성하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @param linkToken 계정 연동 토큰
	 * @return OAuth 인증 URL 정보
	 */
	public OAuthUrlResponseDto generateAuthUrlWithToken(OAuthProvider provider, String linkToken) {
		OAuthProperties.ProviderProperties providerProps = getProviderProperties(provider);
		log.info("URL 생성 시작: provider={}, linkToken={}", provider, linkToken);

		if (provider == OAuthProvider.NAVER) {
			return generateNaverAuthUrl(providerProps, linkToken);
		} else {
			return generateOtherProviderAuthUrl(providerProps, linkToken, provider);
		}
	}

	/**
	 * 인증 코드로 토큰을 획득하고 사용자 정보를 반환합니다.
	 *
	 * @param code 인증 코드
	 * @param provider OAuth 제공자
	 * @return OAuth 사용자 정보
	 */
	private OAuthUserInfoDto getUserInfoFromOAuth(String code, OAuthProvider provider) {
		String accessToken = oAuthClient.getAccessToken(code, provider);
		OAuthUserInfoDto userInfo = oAuthClient.getUserInfo(accessToken, provider);
		log.info("OAuth 사용자 정보 획득: providerId={}, email={}",
			userInfo.getProviderUserId(), userInfo.getEmail());
		return userInfo;
	}

	/**
	 * 기존 OAuth 연동 계정으로 로그인을 처리합니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @param existingOAuth 기존 OAuth 연동 정보
	 * @return OAuth 로그인 결과
	 */
	private OAuthLoginResult handleExistingOAuthLogin(OAuthLoginDto oAuthLoginDto, OAuth existingOAuth) {
		log.info("기존 OAuth 연동 발견: userId={}", existingOAuth.getUserId());

		// 계정 연동 모드인 경우 소유자 검증
		validateOAuthOwnership(oAuthLoginDto, existingOAuth);

		// 기존 OAuth 연동이 있으면 해당 사용자로 로그인
		User user = userMapper.findByUserId(existingOAuth.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

		return generateLoginResult(user);
	}

	/**
	 * OAuth 소유자 검증을 수행합니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @param existingOAuth 기존 OAuth 연동 정보
	 */
	private void validateOAuthOwnership(OAuthLoginDto oAuthLoginDto, OAuth existingOAuth) {
		if (oAuthLoginDto.isLinkAccount() && oAuthLoginDto.getLinkUserId() != null) {
			if (!existingOAuth.getUserId().equals(oAuthLoginDto.getLinkUserId())) {
				log.warn("계정 연동 시도: 다른 사용자의 OAuth 계정 접근 차단 - 요청userId={}, OAuth소유자userId={}",
					oAuthLoginDto.getLinkUserId(), existingOAuth.getUserId());
				throw new ApiException(ErrorCode.CONFLICT_OAUTH_ALREADY_LINKED);
			}
			log.info("계정 연동 모드: 본인 OAuth 계정 확인됨 - userId={}", existingOAuth.getUserId());
		}
	}

	/**
	 * 링크 토큰 기반 계정 연동을 처리합니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @param userInfo OAuth 사용자 정보
	 * @param provider OAuth 제공자
	 * @return OAuth 로그인 결과
	 */
	private OAuthLoginResult handleLinkTokenBasedConnection(OAuthLoginDto oAuthLoginDto,
			OAuthUserInfoDto userInfo, OAuthProvider provider) {
		log.info("링크 토큰 기반 계정 연동 처리: userId={}", oAuthLoginDto.getLinkUserId());

		// 중복 OAuth 연동 확인
		Optional<OAuth> duplicateOAuth = oAuthMapper.findByUserIdAndProvider(
			oAuthLoginDto.getLinkUserId(), provider.name());
		if (duplicateOAuth.isPresent()) {
			log.warn("이미 연동된 OAuth 제공자: userId={}, provider={}",
				oAuthLoginDto.getLinkUserId(), provider);
			throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
		}

		// 사용자 존재 확인 및 연동
		User existingUser = userMapper.findByUserId(oAuthLoginDto.getLinkUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

		linkOAuthToExistingUser(existingUser.getUserId(), userInfo);
		log.info("OAuth 계정 연동 완료: userId={}, provider={}",
			existingUser.getUserId(), provider);

		return generateLoginResult(existingUser);
	}

	/**
	 * 이메일 기반 로그인을 처리합니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 요청 정보
	 * @param userInfo OAuth 사용자 정보
	 * @param provider OAuth 제공자
	 * @return OAuth 로그인 결과 (없으면 null)
	 */
	private OAuthLoginResult handleEmailBasedLogin(OAuthLoginDto oAuthLoginDto,
			OAuthUserInfoDto userInfo, OAuthProvider provider) {
		Optional<User> existingUserByEmail = userMapper.findByEmail(userInfo.getEmail());

		if (existingUserByEmail.isPresent()) {
			// 연동을 원하지 않는 경우
			if (!oAuthLoginDto.isLinkAccount()) {
				return new OAuthLoginResult(
					OAuthResponseDto.builder()
						.isExistingEmail(true)
						.email(userInfo.getEmail())
						.build(),
					null,
					null
				);
			}

			// 중복 OAuth 연동 확인
			Optional<OAuth> duplicateOAuth = oAuthMapper.findByUserIdAndProvider(
					existingUserByEmail.get().getUserId(), provider.name());
			if (duplicateOAuth.isPresent()) {
				throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
			}

			// 기존 계정과 연동
			linkOAuthToExistingUser(existingUserByEmail.get().getUserId(), userInfo);
			return generateLoginResult(existingUserByEmail.get());
		}

		return null;
	}

	/**
	 * 신규 사용자 등록을 처리합니다.
	 *
	 * @param userInfo OAuth 사용자 정보
	 * @return OAuth 로그인 결과
	 */
	private OAuthLoginResult handleNewUserRegistration(OAuthUserInfoDto userInfo) {
		log.info("신규 회원가입 진행: email={}", userInfo.getEmail());
		User newUser = registerNewUser(userInfo);
		linkOAuthToExistingUser(newUser.getUserId(), userInfo);
		log.info("신규 회원가입 완료: userId={}", newUser.getUserId());

		return generateLoginResult(newUser);
	}

	/**
	 * 로그인 결과를 생성합니다.
	 *
	 * @param user 사용자 정보
	 * @return OAuth 로그인 결과
	 */
	private OAuthLoginResult generateLoginResult(User user) {
		// 로그인 시간 업데이트
		userMapper.updateLastLoginByUserId(user.getUserId(), LocalDateTime.now());

		UserRole userRole = userRoleMapper.findByUserId(user.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

		AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(
			user.getUserId(), user.getUserName(), userRole.getRoleId());

		return new OAuthLoginResult(
			OAuthResponseDto.builder().isExistingEmail(false).build(),
			authTokenDto.accessToken(),
			authTokenDto
		);
	}

	/**
	 * 네이버 OAuth 인증 URL을 생성합니다.
	 *
	 * @param providerProps 제공자 프로퍼티
	 * @param linkToken 링크 토큰
	 * @return OAuth URL 응답
	 */
	private OAuthUrlResponseDto generateNaverAuthUrl(
			OAuthProperties.ProviderProperties providerProps, String linkToken) {
		String baseUrl = providerProps.getAuthUri();
		String originalState = providerProps.getState();
		if (originalState == null) {
			originalState = "random_state";
		}
		String stateValue = originalState + "_" + linkToken;
		String authUrl = buildAuthUrl(baseUrl, stateValue);

		log.info("네이버 URL 생성: 원본state={}, 새state={}, authUrl={}",
			providerProps.getState(), stateValue, authUrl);

		return OAuthUrlResponseDto.builder()
			.authUrl(authUrl)
			.provider(OAuthProvider.NAVER.name())
			.state(stateValue)
			.build();
	}

	/**
	 * 네이버가 아닌 다른 제공자의 OAuth 인증 URL을 생성합니다.
	 *
	 * @param providerProps 제공자 프로퍼티
	 * @param linkToken 링크 토큰
	 * @param provider OAuth 제공자
	 * @return OAuth URL 응답
	 */
	private OAuthUrlResponseDto generateOtherProviderAuthUrl(
			OAuthProperties.ProviderProperties providerProps, String linkToken, OAuthProvider provider) {
		String baseUrl = providerProps.getAuthUri();
		String stateValue = "link_" + linkToken;
		String authUrl = buildAuthUrl(baseUrl, stateValue);

		log.info("{} URL 생성: 원본URL={}, 최종URL={}, state={}",
			provider, baseUrl, authUrl, stateValue);

		return OAuthUrlResponseDto.builder()
			.authUrl(authUrl)
			.provider(provider.name())
			.state(stateValue)
			.build();
	}

	/**
	 * OAuth 인증 URL을 구성합니다.
	 *
	 * @param baseUrl 기본 URL
	 * @param stateValue state 값
	 * @return 구성된 인증 URL
	 */
	private String buildAuthUrl(String baseUrl, String stateValue) {
		if (baseUrl.contains(STATE)) {
			return baseUrl.replaceAll("state=[^&]*", STATE
				+ URLEncoder.encode(stateValue, StandardCharsets.UTF_8));
		} else {
			String connector = baseUrl.contains("?") ? "&" : "?";
			return baseUrl + connector + STATE + URLEncoder.encode(stateValue, StandardCharsets.UTF_8);
		}
	}

	/**
	 * 제공자별 프로퍼티를 반환하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @return 제공자 프로퍼티
	 */
	private OAuthProperties.ProviderProperties getProviderProperties(OAuthProvider provider) {
		return switch (provider) {
			case GOOGLE -> oAuthProperties.getGoogle();
			case KAKAO -> oAuthProperties.getKakao();
			case NAVER -> oAuthProperties.getNaver();
			case GITHUB -> oAuthProperties.getGithub();
		};
	}

	/**
	 * OAuth 로그인 처리 결과 및 JWT 토큰을 담는 클래스
	 */
	@Getter
	@AllArgsConstructor
	public static class OAuthLoginResult {
		private OAuthResponseDto responseDto;
		private String jwtToken;
		private AuthTokenDto authTokenDto;
	}
}
