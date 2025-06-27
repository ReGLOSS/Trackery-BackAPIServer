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

		// 인증 코드로 토큰 획득
		String accessToken = oAuthClient.getAccessToken(oAuthLoginDto.getCode(), provider);

		// 액세스 토큰으로 사용자 정보 획득
		OAuthUserInfoDto userInfo = oAuthClient.getUserInfo(accessToken, provider);
		log.info("OAuth 사용자 정보 획득: providerId={}, email={}",
			userInfo.getProviderUserId(), userInfo.getEmail());

		// 1단계: provider + providerId로 기존 OAuth 연동 확인 (최우선)
		Optional<OAuth> existingOAuth = oAuthMapper.findByProviderAndProviderId(provider.name(),
			userInfo.getProviderUserId());

		if (existingOAuth.isPresent()) {
			log.info("기존 OAuth 연동 발견: userId={}", existingOAuth.get().getUserId());
			
			// 계정 연동 모드인 경우, 현재 연동 시도하는 사용자와 OAuth 소유자가 같은지 검증
			if (oAuthLoginDto.isLinkAccount() && oAuthLoginDto.getLinkUserId() != null) {
				if (!existingOAuth.get().getUserId().equals(oAuthLoginDto.getLinkUserId())) {
					log.warn("계정 연동 시도: 다른 사용자의 OAuth 계정 접근 차단 - 요청userId={}, OAuth소유자userId={}", 
						oAuthLoginDto.getLinkUserId(), existingOAuth.get().getUserId());
					throw new ApiException(ErrorCode.CONFLICT_OAUTH_ALREADY_LINKED);
				}
				log.info("계정 연동 모드: 본인 OAuth 계정 확인됨 - userId={}", existingOAuth.get().getUserId());
			}
			
			// 기존 OAuth 연동이 있으면 해당 사용자로 로그인
			User user = userMapper.findByUserId(existingOAuth.get().getUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

			// 로그인 시간 업데이트
			userMapper.updateLastLoginByUserId(user.getUserId(), LocalDateTime.now());

			UserRole userRole = userRoleMapper.findByUserId(user.getUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

			AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(user.getUserId(),
				user.getUserName(), userRole.getRoleId());

			return new OAuthLoginResult(
				OAuthResponseDto.builder().isExistingEmail(false).build(),
				authTokenDto.accessToken(),
				authTokenDto
			);
		}

		// 1.5단계: link_token으로 직접 연동 처리 (linkUserId가 있는 경우)
		if (oAuthLoginDto.isLinkAccount() && oAuthLoginDto.getLinkUserId() != null) {
			log.info("링크 토큰 기반 계정 연동 처리: userId={}", oAuthLoginDto.getLinkUserId());
			// 해당 사용자에게 이미 동일한 OAuth 연동이 있는지 확인
			Optional<OAuth> duplicateOAuth = oAuthMapper.findByUserIdAndProvider(
				oAuthLoginDto.getLinkUserId(), provider.name());
			if (duplicateOAuth.isPresent()) {
				log.warn("이미 연동된 OAuth 제공자: userId={}, provider={}",
					oAuthLoginDto.getLinkUserId(), provider);
				throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
			}

			// 사용자 존재 확인
			User existingUser = userMapper.findByUserId(oAuthLoginDto.getLinkUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

			// 기존 계정과 연동
			linkOAuthToExistingUser(existingUser.getUserId(), userInfo);
			log.info("OAuth 계정 연동 완료: userId={}, provider={}",
				existingUser.getUserId(), provider);

			// 로그인 시간 업데이트
			userMapper.updateLastLoginByUserId(existingUser.getUserId(), LocalDateTime.now());

			UserRole userRole = userRoleMapper.findByUserId(existingUser.getUserId())
				.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

			AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(
				existingUser.getUserId(),
				existingUser.getUserName(),
				userRole.getRoleId()
			);

			return new OAuthLoginResult(
				OAuthResponseDto.builder().isExistingEmail(false).build(),
				authTokenDto.accessToken(),
				authTokenDto
			);
		}

		// 2단계: 이메일로 기존 계정 확인 (이메일이 있는 경우)
		if (userInfo.getEmail() != null && !userInfo.getEmail().trim().isEmpty()) {
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

				// 연동을 원하는 경우 - 해당 사용자에게 이미 동일한 OAuth 연동이 있는지 확인
				Optional<OAuth> duplicateOAuth = oAuthMapper.findByUserIdAndProvider(
					existingUserByEmail.get().getUserId(), provider.name());
				if (duplicateOAuth.isPresent()) {
					throw new ApiException(ErrorCode.DUPLICATE_EMAIL); // 이미 연동된 OAuth 제공자
				}

				// 기존 계정과 연동
				linkOAuthToExistingUser(existingUserByEmail.get().getUserId(), userInfo);

				// 로그인 시간 업데이트
				userMapper.updateLastLoginByUserId(existingUserByEmail.get().getUserId(), LocalDateTime.now());

				UserRole userRole = userRoleMapper.findByUserId(existingUserByEmail.get().getUserId())
					.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

				AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(
					existingUserByEmail.get().getUserId(),
					existingUserByEmail.get().getUserName(),
					userRole.getRoleId()
				);

				return new OAuthLoginResult(
					OAuthResponseDto.builder().isExistingEmail(false).build(),
					authTokenDto.accessToken(),
					authTokenDto
				);
			}
		}

		// 3단계: 연동 모드인데 연동할 기존 계정이 없는 경우 에러
		if (oAuthLoginDto.isLinkAccount()) {
			log.warn("연동 모드이지만 연동할 기존 계정을 찾을 수 없음");
			throw new ApiException(ErrorCode.NOT_FOUND);
		}

		// 4단계: 이메일도 없고 OAuth 연동도 없으면 신규 회원가입 진행
		log.info("신규 회원가입 진행: email={}", userInfo.getEmail());
		User newUser = registerNewUser(userInfo);
		linkOAuthToExistingUser(newUser.getUserId(), userInfo);
		log.info("신규 회원가입 완료: userId={}", newUser.getUserId());

		// 로그인 시간 업데이트 (신규 가입 시에도)
		userMapper.updateLastLoginByUserId(newUser.getUserId(), LocalDateTime.now());

		UserRole userRole = userRoleMapper.findByUserId(newUser.getUserId())
			.orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_SERVER_ERROR));

		AuthTokenDto authTokenDto = jwtService.generateAccessTokenAndRefreshToken(newUser.getUserId(),
			newUser.getUserName(), userRole.getRoleId());

		return new OAuthLoginResult(
			OAuthResponseDto.builder().isExistingEmail(false).build(),
			authTokenDto.accessToken(),
			authTokenDto
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
	 * link_token이 포함된 OAuth 인증 URL을 생성하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @param linkToken 계정 연동 토큰
	 * @return OAuth 인증 URL 정보
	 */
	public OAuthUrlResponseDto generateAuthUrlWithToken(OAuthProvider provider, String linkToken) {
		OAuthProperties.ProviderProperties providerProps = getProviderProperties(provider);
		String baseUrl = providerProps.getAuthUri();
		String authUrl;
		String stateValue;
		log.info("URL 생성 시작: provider={}, linkToken={}", provider, linkToken);
		if (provider == OAuthProvider.NAVER) {
			// 네이버만 state에 link_token 포함
			String originalState = providerProps.getState();
			if (originalState == null) {
				originalState = "random_state";
			}
			stateValue = originalState + "_" + linkToken;
			if (baseUrl.contains(STATE)) {
				authUrl = baseUrl.replaceAll("state=[^&]*", STATE
					+ URLEncoder.encode(stateValue, StandardCharsets.UTF_8));
			} else {
				String connector = baseUrl.contains("?") ? "&" : "?";
				authUrl = baseUrl + connector + STATE + URLEncoder.encode(stateValue, StandardCharsets.UTF_8);
			}
			log.info("네이버 URL 생성: 원본state={}, 새state={}, authUrl={}",
				providerProps.getState(), stateValue, authUrl);
		} else {
			// 다른 제공자들도 state에 link_token 포함 (OAuth 콜백에서 파라미터 유지 안되므로)
			stateValue = "link_" + linkToken;
			if (baseUrl.contains(STATE)) {
				authUrl = baseUrl.replaceAll("state=[^&]*", STATE
					+ URLEncoder.encode(stateValue, StandardCharsets.UTF_8));
			} else {
				String connector = baseUrl.contains("?") ? "&" : "?";
				authUrl = baseUrl + connector + STATE + URLEncoder.encode(stateValue, StandardCharsets.UTF_8);
			}
			log.info("{} URL 생성: 원본URL={}, 최종URL={}, state={}",
				provider, baseUrl, authUrl, stateValue);
		}
		return OAuthUrlResponseDto.builder()
			.authUrl(authUrl)
			.provider(provider.name())
			.state(stateValue)
			.build();
	}

	/**
	 * 제공자별 프로퍼티를 반환하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @return 제공자 프로퍼티
	 */
	private OAuthProperties.ProviderProperties getProviderProperties(OAuthProvider provider) {
		switch (provider) {
			case GOOGLE:
				return oAuthProperties.getGoogle();
			case KAKAO:
				return oAuthProperties.getKakao();
			case NAVER:
				return oAuthProperties.getNaver();
			case GITHUB:
				return oAuthProperties.getGithub();
			default:
				throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		}
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
