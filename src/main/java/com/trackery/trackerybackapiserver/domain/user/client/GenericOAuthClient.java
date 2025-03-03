package com.trackery.trackerybackapiserver.domain.user.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.config.OAuthProperties;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.TokenResponseDto;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.client
 * fileName       : GenericOAuthClient
 * author         : inari
 * date           : 25. 2. 26.
 * description    : 모든 간편 로그인 서비스를 하나의 클래스에서 처리하는 클라이언트입니다.
 * 					간편 로그인 서비스에서 액세스 토큰을 얻고 사용자 정보를 가져옵니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 26.        inari       최초 생성
 * 25. 2. 28.        inari       리프레시 토큰 제거, 깃허브 로그인시 이메일 요청 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericOAuthClient implements OAuthClient {

	/**
	 * HTTP 요청을 위한 RestTemplate 객체
	 */
	private final RestTemplate restTemplate;

	/**
	 * JSON 데이터 파싱을 위한 ObjectMapper 객체
	 */
	private final ObjectMapper objectMapper;

	/**
	 * OAuth 제공자의 설정 정보를 포함하는 프로퍼티 객체
	 */
	private final OAuthProperties oAuthProperties;

	/**
	 * OAuth 제공자별 사용자 정보 파싱을 위한 매핑
	 */
	private final Map<OAuthProvider, Function<JsonNode, OAuthUserInfoDto>> userInfoParsers = new HashMap<>();

	{
		// 네이버 간편 로그인 파서
		userInfoParsers.put(OAuthProvider.NAVER, (jsonNode) -> {
			JsonNode responseNode = jsonNode.get("response");
			String id = responseNode.get("id").asText();
			String email = responseNode.has("email") ? responseNode.get("email").asText() : null;
			String nickname = responseNode.has("nickname") ? responseNode.get("nickname").asText() : null;
			String profileImage = responseNode.has("profileImage") ? responseNode.get("profileImage").asText() : null;

			return OAuthUserInfoDto.builder()
				.email(email)
				.nickname(nickname)
				.userprofile(profileImage)
				.provider(OAuthProvider.NAVER.name())
				.providerUserId(id)
				.build();
		});

		// 카카오 간편 로그인 파서
		userInfoParsers.put(OAuthProvider.KAKAO, (jsonNode) -> {
			JsonNode kakaoAccount = jsonNode.get("kakao_account");
			JsonNode profile = kakaoAccount.get("profile");

			String id = jsonNode.get("id").asText();
			String email = kakaoAccount.has("email") ? kakaoAccount.get("email").asText() : null;
			String nickname = profile.has("nickname") ? profile.get("nickname").asText() : null;
			String profileImage = profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : null;

			return OAuthUserInfoDto.builder()
				.email(email)
				.nickname(nickname)
				.userprofile(profileImage)
				.provider(OAuthProvider.KAKAO.name())
				.providerUserId(id)
				.build();
		});

		// 구글 간편 로그인 파서
		userInfoParsers.put(OAuthProvider.GOOGLE, (jsonNode) -> {
			String id = jsonNode.get("sub").asText();
			String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
			String nickname = jsonNode.has("name") ? jsonNode.get("name").asText() : null;
			String profileImage = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;

			return OAuthUserInfoDto.builder()
				.email(email)
				.nickname(nickname)
				.userprofile(profileImage)
				.provider(OAuthProvider.GOOGLE.name())
				.providerUserId(id)
				.build();
		});

		// 깃허브 간편 로그인 파서
		userInfoParsers.put(OAuthProvider.GITHUB, (jsonNode) -> {
			String id = jsonNode.get("id").asText();

			// email이 null이나 빈 문자열인 경우가 많음
			String email = jsonNode.has("email") && !jsonNode.get("email").isNull() ?
				jsonNode.get("email").asText() : null;

			// 로깅 추가
			log.debug("GitHub 사용자 정보 원본: {}", jsonNode.toString());
			log.debug("GitHub 이메일 (기본 응답): {}", email);

			String nickname = jsonNode.has("login") ? jsonNode.get("login").asText() : null;
			String profileImage = jsonNode.has("avatar_url") ? jsonNode.get("avatar_url").asText() : null;

			return OAuthUserInfoDto.builder()
				.email(email) // 기본 응답의 이메일 (나중에 보완될 수 있음)
				.nickname(nickname)
				.userprofile(profileImage)
				.provider(OAuthProvider.GITHUB.name())
				.providerUserId(id)
				.build();
		});
	}

	/**
	 * 인증 코드로 액세스 토큰을 획득하는 메서드입니다.
	 *
	 * @param code 일회성 인증 코드
	 * @param provider 간편 로그인 제공자
	 * @return 액세스 토큰 포함한 응답
	 */
	public TokenResponseDto getTokens(String code, String provider) {
		try {
			OAuthProvider oAuthProvider = OAuthProvider.valueOf(provider.toUpperCase());
			OAuthProperties.ProviderProperties properties = getProviderProperties(oAuthProvider);

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			// GitHub OAuth API는 Accept 헤더가 필요함
			if (oAuthProvider == OAuthProvider.GITHUB) {
				headers.set("Accept", "application/json");
				headers.setContentType(MediaType.APPLICATION_JSON);

				// GitHub API는 JSON 형식으로 요청
				Map<String, String> requestBody = new HashMap<>();
				requestBody.put("client_id", properties.getClientId());
				requestBody.put("client_secret", properties.getClientSecret());
				requestBody.put("code", code);
				requestBody.put("redirect_uri", properties.getRedirectUri());

				HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
				log.info("GitHub OAuth 토큰 요청: URI: {}", properties.getTokenUri());
				String response = restTemplate.postForObject(properties.getTokenUri(), request, String.class);
				log.debug("GitHub OAuth 응답: {}", response);

				JsonNode jsonNode = objectMapper.readTree(response);

				if (!jsonNode.has("access_token")) {
					log.error("깃허브 액세스 토큰이 응답에 없습니다. 응답: {}", response);
					throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
				}

				String accessToken = jsonNode.get("access_token").asText();
				return new TokenResponseDto(accessToken);
			} else {
				// 기존 다른 OAuth 제공자 처리 로직 유지
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

				MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
				body.add("grant_type", "authorization_code");
				body.add("client_id", properties.getClientId());
				body.add("client_secret", properties.getClientSecret());
				body.add("code", code);
				body.add("redirect_uri", properties.getRedirectUri());

				if (oAuthProvider == OAuthProvider.NAVER && properties.getState() != null) {
					body.add("state", properties.getState());
				}

				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
				log.info("OAuth 토큰 요청: {}, URI: {}", provider, properties.getTokenUri());
				String response = restTemplate.postForObject(properties.getTokenUri(), request, String.class);
				log.debug("OAuth 응답: {}", response);

				JsonNode jsonNode = objectMapper.readTree(response);

				if (!jsonNode.has("access_token")) {
					log.error("액세스 토큰이 응답에 없습니다. 응답: {}", response);
					throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
				}

				String accessToken = jsonNode.get("access_token").asText();
				return new TokenResponseDto(accessToken);
			}
		} catch (Exception e) {
			log.error("OAuth 토큰 획득 실패: {}, 오류: {}", provider, e.getMessage(), e);
			throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
		}
	}

	/**
	 * 일회성 인증 코드로 액세스 토큰을 획득하는 메서드
	 *
	 * @param code 일회성 인증 코드
	 * @param provider 간편 로그인 제공자
	 * @return 액세스 토큰
	 */
	@Override
	public String getAccessToken(String code, String provider) {
		return getTokens(code, provider).getAccessToken();
	}

	/**
	 * 액세스 토큰으로 사용자 정보를 획득하는 메서드입니다.
	 *
	 * @param accessToken 액세스 토큰
	 * @param provider 간편 로그인 제공자
	 * @return 사용자 정보
	 */
	@Override
	public OAuthUserInfoDto getUserInfo(String accessToken, String provider) {
		try {
			OAuthProvider oAuthProvider = OAuthProvider.valueOf(provider.toUpperCase());
			OAuthProperties.ProviderProperties properties = getProviderProperties(oAuthProvider);

			HttpHeaders headers = new HttpHeaders();

			switch (oAuthProvider) {
				case NAVER:
					headers.setBearerAuth(accessToken);
					break;
				case KAKAO:
					headers.setBearerAuth(accessToken);
					headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
					break;
				case GOOGLE:
					headers.setBearerAuth(accessToken);
					break;
				case GITHUB:
					headers.setBearerAuth(accessToken);
					headers.set("Accept", "application/json");
					break;
			}

			HttpEntity<Void> request = new HttpEntity<>(headers);
			log.debug("OAuth 사용자 정보 요청: {}, URI: {}, Headers: {}", provider, properties.getUserInfoUri(), headers);
			String response = restTemplate.exchange(
				properties.getUserInfoUri(), HttpMethod.GET, request, String.class).getBody();
			log.debug("OAuth 사용자 정보 응답: {}", response);

			JsonNode jsonNode = objectMapper.readTree(response);

			// userInfoParser를 통해 사용자 정보 파싱
			Function<JsonNode, OAuthUserInfoDto> userInfoParser = userInfoParsers.get(oAuthProvider);
			if (userInfoParser != null) {
				OAuthUserInfoDto userInfo = userInfoParser.apply(jsonNode);

				// 깃허브의 경우 이메일이 null이면 이메일 API를 추가로 호출
				if (oAuthProvider == OAuthProvider.GITHUB &&
					(userInfo.getEmail() == null || userInfo.getEmail().isEmpty())) {

					log.info("GitHub 이메일이 없습니다. 이메일 API 호출을 시도합니다.");

					try {
						// /user/emails API 호출하여 이메일 목록 가져오기
						HttpEntity<Void> emailRequest = new HttpEntity<>(headers);
						String emailResponse = restTemplate.exchange(
							"https://api.github.com/user/emails",
							HttpMethod.GET,
							emailRequest,
							String.class
						).getBody();

						log.debug("GitHub 이메일 API 응답: {}", emailResponse);

						// 이메일 목록에서 기본(primary) 이메일 찾기
						JsonNode emailsNode = objectMapper.readTree(emailResponse);
						if (emailsNode.isArray() && emailsNode.size() > 0) {
							String primaryEmail = null;

							// 먼저 primary=true인 이메일 찾기
							for (JsonNode emailNode : emailsNode) {
								if (emailNode.has("primary") && emailNode.get("primary").asBoolean() &&
									emailNode.has("email")) {
									primaryEmail = emailNode.get("email").asText();
									log.info("GitHub primary 이메일 찾음: {}", primaryEmail);
									break;
								}
							}

							// primary 이메일이 없으면 첫 번째 이메일 사용
							if (primaryEmail == null && emailsNode.size() > 0 &&
								emailsNode.get(0).has("email")) {
								primaryEmail = emailsNode.get(0).get("email").asText();
								log.info("GitHub primary 이메일이 없어 첫 번째 이메일 사용: {}", primaryEmail);
							}

							// 이메일을 찾았으면 새로운 DTO 생성
							if (primaryEmail != null) {
								userInfo = OAuthUserInfoDto.builder()
									.email(primaryEmail)
									.nickname(userInfo.getNickname())
									.userprofile(userInfo.getUserprofile())
									.provider(userInfo.getProvider())
									.providerUserId(userInfo.getProviderUserId())
									.build();
							}
						}
					} catch (Exception e) {
						log.error("GitHub 이메일 API 호출 실패: {}", e.getMessage(), e);
					}
				}

				return userInfo;
			} else {
				throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
			}
		} catch (Exception e) {
			log.error("OAuth 사용자 정보 획득 실패: {}, 오류: {}", provider, e.getMessage(), e);
			throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
		}
	}

	/**
	 * 간편 로그인 제공자 속성을 가져오는 메서드
	 *
	 * @param provider 간편 로그인 제공자
	 * @return 제공자별 속성
	 */
	private OAuthProperties.ProviderProperties getProviderProperties(OAuthProvider provider) {
		switch (provider) {
			case NAVER:
				return oAuthProperties.getNaver();
			case KAKAO:
				return oAuthProperties.getKakao();
			case GOOGLE:
				return oAuthProperties.getGoogle();
			case GITHUB:
				return oAuthProperties.getGithub();
			default:
				throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		}
	}

}
