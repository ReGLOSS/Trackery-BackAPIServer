package com.trackery.trackerybackapiserver.domain.user.client;

import java.util.EnumMap;
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
 * 25. 3. 05.        inari       프로필 사진 제거
 * 25. 3. 20.        inari       소나큐브 코드스멜 개선
 * 25. 3. 27.        inari		 provider를 enum으로 변경
 * 25. 6. 25.        inari		 코드 스멜 수정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericOAuthClient implements OAuthClient {

	/**
	 * 사용자 정보 필드 상수
	 */
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_NICKNAME = "nickname";
	private static final String FIELD_ID = "id";
	private static final String FIELD_ACCESS_TOKEN = "access_token";
	private static final String FIELD_PRIMARY = "primary";

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
	private final Map<OAuthProvider, Function<JsonNode, OAuthUserInfoDto>> userInfoParsers
		= createUserInfoParsers();

	private Map<OAuthProvider, Function<JsonNode, OAuthUserInfoDto>> createUserInfoParsers() {
		Map<OAuthProvider, Function<JsonNode, OAuthUserInfoDto>> parsers = new EnumMap<>(OAuthProvider.class);
		parsers.put(OAuthProvider.NAVER, this::parseNaverUserInfo);
		parsers.put(OAuthProvider.KAKAO, this::parseKakaoUserInfo);
		parsers.put(OAuthProvider.GOOGLE, this::parseGoogleUserInfo);
		parsers.put(OAuthProvider.GITHUB, this::parseGithubUserInfo);

		return parsers;
	}

	/**
	 * 네이버 간편 로그인 사용자 정보를 파싱하는 메서드입니다.
	 *
	 * @param jsonNode 네이버 API 응답 JSON
	 * @return 파싱된 사용자 정보
	 */
	private OAuthUserInfoDto parseNaverUserInfo(JsonNode jsonNode) {
		JsonNode responseNode = jsonNode.get("response");
		String id = responseNode.get(FIELD_ID).asText();
		String email = extractOptionalField(responseNode, FIELD_EMAIL);
		String nickname = extractOptionalField(responseNode, FIELD_NICKNAME);

		return OAuthUserInfoDto.builder()
			.email(email)
			.nickname(nickname)
			.provider(OAuthProvider.NAVER.name())
			.providerUserId(id)
			.build();
	}

	/**
	 * 카카오 간편 로그인 사용자 정보를 파싱하는 메서드입니다.
	 *
	 * @param jsonNode 카카오 API 응답 JSON
	 * @return 파싱된 사용자 정보
	 */
	private OAuthUserInfoDto parseKakaoUserInfo(JsonNode jsonNode) {
		JsonNode kakaoAccount = jsonNode.get("kakao_account");
		JsonNode profile = kakaoAccount.get("profile");

		String id = jsonNode.get(FIELD_ID).asText();
		String email = extractOptionalField(kakaoAccount, FIELD_EMAIL);
		String nickname = extractOptionalField(profile, FIELD_NICKNAME);

		return OAuthUserInfoDto.builder()
			.email(email)
			.nickname(nickname)
			.provider(OAuthProvider.KAKAO.name())
			.providerUserId(id)
			.build();
	}

	/**
	 * 구글 간편 로그인 사용자 정보를 파싱하는 메서드입니다.
	 *
	 * @param jsonNode 구글 API 응답 JSON
	 * @return 파싱된 사용자 정보
	 */
	private OAuthUserInfoDto parseGoogleUserInfo(JsonNode jsonNode) {
		String id = jsonNode.get("sub").asText();
		String email = extractOptionalField(jsonNode, FIELD_EMAIL);
		String nickname = extractOptionalField(jsonNode, "name");

		return OAuthUserInfoDto.builder()
			.email(email)
			.nickname(nickname)
			.provider(OAuthProvider.GOOGLE.name())
			.providerUserId(id)
			.build();
	}

	/**
	 * 깃허브 간편 로그인 사용자 정보를 파싱하는 메서드입니다.
	 *
	 * @param jsonNode 깃허브 API 응답 JSON
	 * @return 파싱된 사용자 정보
	 */
	private OAuthUserInfoDto parseGithubUserInfo(JsonNode jsonNode) {
		String id = jsonNode.get(FIELD_ID).asText();
		String email = extractGithubEmail(jsonNode);
		String nickname = extractOptionalField(jsonNode, "login");

		log.debug("GitHub 사용자 정보 원본: {}", jsonNode);
		log.debug("GitHub 이메일 (기본 응답): {}", email);

		return OAuthUserInfoDto.builder()
			.email(email)
			.nickname(nickname)
			.provider(OAuthProvider.GITHUB.name())
			.providerUserId(id)
			.build();
	}

	/**
	 * JSON 노드에서 선택적 필드를 추출하는 메서드입니다.
	 * 필드가 존재하지 않으면 null을 반환합니다.
	 *
	 * @param node JSON 노드
	 * @param fieldName 추출할 필드명
	 * @return 필드 값 또는 null
	 */
	private String extractOptionalField(JsonNode node, String fieldName) {
		return node.has(fieldName) ? node.get(fieldName).asText() : null;
	}

	/**
	 * 깃허브 사용자 정보에서 이메일을 추출하는 메서드입니다.
	 * 깃허브는 이메일이 null이거나 비공개일 수 있어 특별한 처리가 필요합니다.
	 *
	 * @param jsonNode 깃허브 사용자 정보 JSON
	 * @return 이메일 주소 또는 null
	 */
	private String extractGithubEmail(JsonNode jsonNode) {
		return jsonNode.has(FIELD_EMAIL) && !jsonNode.get(FIELD_EMAIL).isNull()
			? jsonNode.get(FIELD_EMAIL).asText() : null;
	}

	/**
	 * 인증 코드로 액세스 토큰을 획득하는 메서드입니다.
	 *
	 * @param code 일회성 인증 코드
	 * @param provider 간편 로그인 제공자
	 * @return 액세스 토큰 포함한 응답
	 */
	public TokenResponseDto getTokens(String code, OAuthProvider provider) {
		try {
			OAuthProperties.ProviderProperties properties = getProviderProperties(provider);

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			// GitHub OAuth API는 Accept 헤더가 필요함
			if (provider == OAuthProvider.GITHUB) {
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

				if (!jsonNode.has(FIELD_ACCESS_TOKEN)) {
					log.error("깃허브 액세스 토큰이 응답에 없습니다. 응답: {}", response);
					throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
				}

				String accessToken = jsonNode.get(FIELD_ACCESS_TOKEN).asText();
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

				if (provider == OAuthProvider.NAVER && properties.getState() != null) {
					body.add("state", properties.getState());
				}

				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
				log.info("OAuth 토큰 요청: {}, URI: {}", provider, properties.getTokenUri());
				String response = restTemplate.postForObject(properties.getTokenUri(), request, String.class);
				log.debug("OAuth 응답: {}", response);

				JsonNode jsonNode = objectMapper.readTree(response);

				if (!jsonNode.has(FIELD_ACCESS_TOKEN)) {
					log.error("액세스 토큰이 응답에 없습니다. 응답: {}", response);
					throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
				}

				String accessToken = jsonNode.get(FIELD_ACCESS_TOKEN).asText();
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
	public String getAccessToken(String code, OAuthProvider provider) {
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
	public OAuthUserInfoDto getUserInfo(String accessToken, OAuthProvider provider) {
		try {
			OAuthProperties.ProviderProperties properties = getProviderProperties(provider);

			// HTTP 헤더 설정
			HttpHeaders headers = configureHeaders(accessToken, provider);

			// 사용자 정보 요청 및 파싱
			JsonNode userInfoJson = requestUserInfo(headers, properties, provider.name());

			// 제공자별 파싱 함수를 통해 사용자 정보 추출
			OAuthUserInfoDto userInfo = parseUserInfo(userInfoJson, provider);

			// GitHub 사용자의 경우 이메일 보완 처리
			if (provider == OAuthProvider.GITHUB && isEmailMissing(userInfo)) {
				userInfo = completeGithubEmail(userInfo, headers);
			}

			return userInfo;
		} catch (Exception e) {
			log.error("OAuth 사용자 정보 획득 실패: {}, 오류: {}", provider, e.getMessage(), e);
			throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
		}
	}

	/**
	 * 제공자별 HTTP 헤더 설정입니다.
	 * 카카오는 API를 폼 데이터 형식으로 처리하며,
	 * 깃허브는 "Accept", "application/json"으로 처리합니다.
	 * 네이버와 구글은 기본 인증만으로 충분하므로 헤더 인증을 따로 하지 않습니다.
	 *
	 * @param accessToken 액세스토큰
	 * @param provider 간편 로그인 제공자
	 * @return 헤더
	 */
	private HttpHeaders configureHeaders(String accessToken, OAuthProvider provider) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		if (provider == OAuthProvider.KAKAO) {
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		} else if (provider == OAuthProvider.GITHUB) {
			headers.set("Accept", "application/json");
		}

		return headers;
	}

	/**
	 * 사용자 정보 API 요청 수행
	 */
	private JsonNode requestUserInfo(
		HttpHeaders headers, OAuthProperties.ProviderProperties properties, String provider) {
		try {
			HttpEntity<Void> request = new HttpEntity<>(headers);
			log.debug("OAuth 사용자 정보 요청: {}, URI: {}, Headers: {}", provider, properties.getUserInfoUri(), headers);

			String response = restTemplate.exchange(
				properties.getUserInfoUri(), HttpMethod.GET, request, String.class).getBody();
			log.debug("OAuth 사용자 정보 응답: {}", response);

			return objectMapper.readTree(response);
		} catch (Exception e) {
			log.error("사용자 정보 요청 실패: {}", e.getMessage());
			throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
		}
	}

	/**
	 * 사용자 정보 파싱
	 */
	private OAuthUserInfoDto parseUserInfo(JsonNode jsonNode, OAuthProvider provider) {
		Function<JsonNode, OAuthUserInfoDto> userInfoParser = userInfoParsers.get(provider);
		if (userInfoParser == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		}

		return userInfoParser.apply(jsonNode);
	}

	/**
	 * 이메일 정보 누락을 확인하는 메서드입니다.
	 */
	private boolean isEmailMissing(OAuthUserInfoDto userInfo) {
		return userInfo.getEmail() == null || userInfo.getEmail().isEmpty();
	}

	/**
	 * GitHub 사용자의 이메일 정보 보완하는 메서드입니다.
	 *
	 * @throws ApiException 이메일이 필수인데 찾지 못한 경우 발생
	 */
	private OAuthUserInfoDto completeGithubEmail(OAuthUserInfoDto userInfo, HttpHeaders headers) {
		log.info("GitHub 이메일이 없습니다. 이메일 API 호출을 시도합니다.");

		JsonNode emailsNode = requestGithubEmails(headers);
		String primaryEmail = findPrimaryEmail(emailsNode);

		if (primaryEmail == null) {
			log.error("GitHub 계정에서 이메일을 찾을 수 없습니다.");
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_INPUT_GITHUB);
		}

		return updateUserInfoWithEmail(userInfo, primaryEmail);
	}

	/**
	 * GitHub 이메일 목록 요청하는 메서드입니다.
	 *
	 * @throws ApiException 이메일 요청 실패 시 발생
	 */
	private JsonNode requestGithubEmails(HttpHeaders headers) {
		try {
			HttpEntity<Void> emailRequest = new HttpEntity<>(headers);
			String emailResponse = restTemplate.exchange(
				"https://api.github.com/user/emails",
				HttpMethod.GET,
				emailRequest,
				String.class
			).getBody();

			log.debug("GitHub 이메일 API 응답: {}", emailResponse);
			return objectMapper.readTree(emailResponse);
		} catch (Exception e) {
			log.error("GitHub 이메일 API 요청 실패: {}", e.getMessage(), e);
			throw new ApiException(ErrorCode.UNAUTHORIZED_OAUTH_FAILED);
		}
	}

	/**
	 * GitHub 이메일 목록에서 적절한 이메일 찾는 메서드입니다.
	 */
	private String findPrimaryEmail(JsonNode emailsNode) {
		if (!emailsNode.isArray() || emailsNode.isEmpty()) {
			return null;
		}

		// 먼저 primary=true인 이메일 찾기
		for (JsonNode emailNode : emailsNode) {
			if (emailNode.has(FIELD_PRIMARY) && emailNode.get(FIELD_PRIMARY).asBoolean()
				&& emailNode.has(FIELD_EMAIL)) {
				String email = emailNode.get(FIELD_EMAIL).asText();
				log.info("GitHub primary 이메일 찾음: {}", email);
				return email;
			}
		}

		// primary 이메일이 없으면 첫 번째 이메일 사용
		if (emailsNode.get(0).has(FIELD_EMAIL)) {
			String email = emailsNode.get(0).get(FIELD_EMAIL).asText();
			log.info("GitHub primary 이메일이 없어 첫 번째 이메일 사용: {}", email);
			return email;
		}

		return null;
	}

	/**
	 * 찾은 이메일로 사용자 정보를 업데이트하는 메서드입니다.
	 */
	private OAuthUserInfoDto updateUserInfoWithEmail(OAuthUserInfoDto userInfo, String email) {
		return OAuthUserInfoDto.builder()
			.email(email)
			.nickname(userInfo.getNickname())
			.provider(userInfo.getProvider())
			.providerUserId(userInfo.getProviderUserId())
			.build();
	}

	/**
	 * 간편 로그인 제공자 속성을 가져오는 메서드입니다.
	 *
	 * @param provider 간편 로그인 제공자
	 * @return 제공자별 속성
	 */
	private OAuthProperties.ProviderProperties getProviderProperties(OAuthProvider provider) {
		if (provider == null) {
			throw new ApiException(ErrorCode.BAD_REQUEST_INVALID_OAUTH_PROVIDER);
		}
		return switch (provider) {
			case NAVER -> oAuthProperties.getNaver();
			case KAKAO -> oAuthProperties.getKakao();
			case GOOGLE -> oAuthProperties.getGoogle();
			case GITHUB -> oAuthProperties.getGithub();
		};
	}
}
