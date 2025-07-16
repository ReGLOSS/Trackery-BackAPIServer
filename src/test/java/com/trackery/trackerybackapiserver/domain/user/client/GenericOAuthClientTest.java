package com.trackery.trackerybackapiserver.domain.user.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trackery.trackerybackapiserver.config.OAuthProperties;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.client
 * fileName       : GenericOAuthClientTest
 * author         : inari
 * date           : 25. 3. 3.
 * description    : GenericOAuthClient 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.		inari		최초 생성
 * 25. 3. 3.		inari		테스트코드 추가
 * 25. 3. 5.		inari		간편로그인시 프로필사진 테스트 제거
 * 25. 3. 14.		durururuk		리팩터링
 * 25. 3. 27.		inari		상세 주석 추가 및 테스트코드 수정
 */
@ExtendWith(MockitoExtension.class)
class GenericOAuthClientTest {

	// 실제 ObjectMapper 사용
	private final ObjectMapper realObjectMapper = new ObjectMapper();
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private OAuthProperties oAuthProperties;
	@InjectMocks
	private GenericOAuthClient genericOAuthClient;

	// 각 OAuth 제공자별로 중첩 클래스 생성
	@Nested
	@DisplayName("네이버 OAuth 테스트")
	class NaverOAuthTest {

		@Mock
		private OAuthProperties.ProviderProperties naverProperties;

		@Test
		@DisplayName("네이버 액세스 토큰 획득 테스트")
		void getAccessToken_Success() throws Exception {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = OAuthProvider.NAVER;
			String accessToken = "naver-access-token";

			// 네이버 속성 설정
			when(oAuthProperties.getNaver()).thenReturn(naverProperties);
			when(naverProperties.getClientId()).thenReturn("naver-client-id");
			when(naverProperties.getClientSecret()).thenReturn("naver-client-secret");
			when(naverProperties.getRedirectUri()).thenReturn("naver-redirect-uri");
			when(naverProperties.getTokenUri()).thenReturn("naver-token-uri");
			when(naverProperties.getState()).thenReturn("naver-state");

			// JSON 응답 준비
			ObjectNode tokenResponse = realObjectMapper.createObjectNode();
			tokenResponse.put("access_token", accessToken);

			String tokenResponseString = realObjectMapper.writeValueAsString(tokenResponse);

			// Mockito 설정
			when(restTemplate.postForObject(eq("naver-token-uri"), any(HttpEntity.class), eq(String.class)))
				.thenReturn(tokenResponseString);
			when(objectMapper.readTree(tokenResponseString)).thenReturn(tokenResponse);

			// when
			String result = genericOAuthClient.getAccessToken(code, provider);

			// then
			assertEquals(accessToken, result);

			// 토큰 요청 파라미터 검증
			ArgumentCaptor<HttpEntity<?>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
			verify(restTemplate).postForObject(eq("naver-token-uri"), requestCaptor.capture(), eq(String.class));

			// 토큰 요청 API가 호출되었는지 검증
			verify(restTemplate).postForObject(
				eq("naver-token-uri"),
				any(HttpEntity.class),
				eq(String.class)
			);
		}

		@Test
		@DisplayName("네이버 사용자 정보 획득 테스트")
		void getUserInfo_Success() throws Exception {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.NAVER;

			// 네이버 속성 설정
			when(oAuthProperties.getNaver()).thenReturn(naverProperties);
			when(naverProperties.getUserInfoUri()).thenReturn("naver-user-info-uri");

			// 네이버 사용자 정보 응답 준비
			String userInfoResponse = "{"
				+ "\"resultcode\":\"00\","
				+ "\"message\":\"success\","
				+ "\"response\":{"
				+ "\"id\":\"12345\","
				+ "\"email\":\"test@naver.com\","
				+ "\"nickname\":\"네이버닉네임\","
				+ "\"profileImage\":\"http://example.com/profile.jpg\""
				+ "}"
				+ "}";

			JsonNode userInfoNode = realObjectMapper.readTree(userInfoResponse);

			ResponseEntity<String> responseEntity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);

			when(restTemplate.exchange(
				eq("naver-user-info-uri"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(responseEntity);

			when(objectMapper.readTree(userInfoResponse)).thenReturn(userInfoNode);

			// when
			OAuthUserInfoDto result = genericOAuthClient.getUserInfo(accessToken, provider);

			// then
			assertEquals("test@naver.com", result.getEmail());
			assertEquals("네이버닉네임", result.getNickname());
			assertEquals("NAVER", result.getProvider());
			assertEquals("12345", result.getProviderUserId());

			// 헤더에 Bearer 토큰이 설정되었는지 검증
			ArgumentCaptor<HttpEntity<?>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
			verify(restTemplate).exchange(
				eq("naver-user-info-uri"),
				eq(HttpMethod.GET),
				requestCaptor.capture(),
				eq(String.class));

			String authHeader = requestCaptor.getValue().getHeaders().getFirst("Authorization");
			assertEquals("Bearer test-access-token", authHeader);
		}
	}

	@Nested
	@DisplayName("카카오 OAuth 테스트")
	class KakaoOAuthTest {

		@Mock
		private OAuthProperties.ProviderProperties kakaoProperties;

		@Test
		@DisplayName("카카오 액세스 토큰 획득 테스트")
		void getAccessToken_Success() throws Exception {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = OAuthProvider.KAKAO;
			String accessToken = "kakao-access-token";

			// 카카오 속성 설정
			when(oAuthProperties.getKakao()).thenReturn(kakaoProperties);
			when(kakaoProperties.getClientId()).thenReturn("kakao-client-id");
			when(kakaoProperties.getClientSecret()).thenReturn("kakao-client-secret");
			when(kakaoProperties.getRedirectUri()).thenReturn("kakao-redirect-uri");
			when(kakaoProperties.getTokenUri()).thenReturn("kakao-token-uri");

			// JSON 응답 준비
			ObjectNode tokenResponse = realObjectMapper.createObjectNode();
			tokenResponse.put("access_token", accessToken);

			String tokenResponseString = realObjectMapper.writeValueAsString(tokenResponse);

			// Mockito 설정
			when(restTemplate.postForObject(eq("kakao-token-uri"), any(HttpEntity.class), eq(String.class)))
				.thenReturn(tokenResponseString);
			when(objectMapper.readTree(tokenResponseString)).thenReturn(tokenResponse);

			// when
			String result = genericOAuthClient.getAccessToken(code, provider);

			// then
			assertEquals(accessToken, result);
		}

		@Test
		@DisplayName("카카오 사용자 정보 획득 테스트")
		void getUserInfo_Success() throws Exception {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.KAKAO;

			// 카카오 속성 설정
			when(oAuthProperties.getKakao()).thenReturn(kakaoProperties);
			when(kakaoProperties.getUserInfoUri()).thenReturn("kakao-user-info-uri");

			// 카카오 사용자 정보 응답 준비
			String userInfoResponse = "{"
				+ "\"id\":12345,"
				+ "\"kakao_account\":{"
				+ "\"email\":\"test@kakao.com\","
				+ "\"profile\":{"
				+ "\"nickname\":\"카카오닉네임\","
				+ "\"profile_image_url\":\"http://example.com/profile.jpg\""
				+ "}"
				+ "}"
				+ "}";

			JsonNode userInfoNode = realObjectMapper.readTree(userInfoResponse);

			ResponseEntity<String> responseEntity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);

			when(restTemplate.exchange(
				eq("kakao-user-info-uri"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(responseEntity);

			when(objectMapper.readTree(userInfoResponse)).thenReturn(userInfoNode);

			// when
			OAuthUserInfoDto result = genericOAuthClient.getUserInfo(accessToken, provider);

			// then
			assertEquals("test@kakao.com", result.getEmail());
			assertEquals("카카오닉네임", result.getNickname());
			assertEquals("KAKAO", result.getProvider());
			assertEquals("12345", result.getProviderUserId());
		}
	}

	@Nested
	@DisplayName("구글 OAuth 테스트")
	class GoogleOAuthTest {

		@Mock
		private OAuthProperties.ProviderProperties googleProperties;

		@Test
		@DisplayName("구글 액세스 토큰 획득 테스트")
		void getAccessToken_Success() throws Exception {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = OAuthProvider.GOOGLE;
			String accessToken = "google-access-token";

			// 구글 속성 설정
			when(oAuthProperties.getGoogle()).thenReturn(googleProperties);
			when(googleProperties.getClientId()).thenReturn("google-client-id");
			when(googleProperties.getClientSecret()).thenReturn("google-client-secret");
			when(googleProperties.getRedirectUri()).thenReturn("google-redirect-uri");
			when(googleProperties.getTokenUri()).thenReturn("google-token-uri");

			// JSON 응답 준비
			ObjectNode tokenResponse = realObjectMapper.createObjectNode();
			tokenResponse.put("access_token", accessToken);

			String tokenResponseString = realObjectMapper.writeValueAsString(tokenResponse);

			// Mockito 설정
			when(restTemplate.postForObject(eq("google-token-uri"), any(HttpEntity.class), eq(String.class)))
				.thenReturn(tokenResponseString);
			when(objectMapper.readTree(tokenResponseString)).thenReturn(tokenResponse);

			// when
			String result = genericOAuthClient.getAccessToken(code, provider);

			// then
			assertEquals(accessToken, result);
		}

		@Test
		@DisplayName("구글 사용자 정보 획득 테스트")
		void getUserInfo_Success() throws Exception {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.GOOGLE;

			// 구글 속성 설정
			when(oAuthProperties.getGoogle()).thenReturn(googleProperties);
			when(googleProperties.getUserInfoUri()).thenReturn("google-user-info-uri");

			// 구글 사용자 정보 응답 준비
			String userInfoResponse = "{"
				+ "\"sub\":\"12345\","
				+ "\"email\":\"test@gmail.com\","
				+ "\"name\":\"구글닉네임\","
				+ "\"picture\":\"http://example.com/profile.jpg\""
				+ "}";

			JsonNode userInfoNode = realObjectMapper.readTree(userInfoResponse);

			ResponseEntity<String> responseEntity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);

			when(restTemplate.exchange(
				eq("google-user-info-uri"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(responseEntity);

			when(objectMapper.readTree(userInfoResponse)).thenReturn(userInfoNode);

			// when
			OAuthUserInfoDto result = genericOAuthClient.getUserInfo(accessToken, provider);

			// then
			assertEquals("test@gmail.com", result.getEmail());
			assertEquals("구글닉네임", result.getNickname());
			assertEquals("GOOGLE", result.getProvider());
			assertEquals("12345", result.getProviderUserId());
		}
	}

	@Nested
	@DisplayName("깃허브 OAuth 테스트")
	class GithubOAuthTest {

		@Mock
		private OAuthProperties.ProviderProperties githubProperties;

		@Test
		@DisplayName("깃허브 액세스 토큰 획득 테스트")
		void getAccessToken_Success() throws Exception {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = OAuthProvider.GITHUB;
			String accessToken = "github-access-token";

			// 깃허브 속성 설정
			when(oAuthProperties.getGithub()).thenReturn(githubProperties);
			when(githubProperties.getClientId()).thenReturn("github-client-id");
			when(githubProperties.getClientSecret()).thenReturn("github-client-secret");
			when(githubProperties.getRedirectUri()).thenReturn("github-redirect-uri");
			when(githubProperties.getTokenUri()).thenReturn("github-token-uri");

			// JSON 응답 준비
			ObjectNode tokenResponse = realObjectMapper.createObjectNode();
			tokenResponse.put("access_token", accessToken);

			String tokenResponseString = realObjectMapper.writeValueAsString(tokenResponse);

			// Mockito 설정
			when(restTemplate.postForObject(eq("github-token-uri"), any(HttpEntity.class), eq(String.class)))
				.thenReturn(tokenResponseString);
			when(objectMapper.readTree(tokenResponseString)).thenReturn(tokenResponse);

			// when
			String result = genericOAuthClient.getAccessToken(code, provider);

			// then
			assertEquals(accessToken, result);

			// 깃허브는 JSON 형식으로 요청하는지 검증
			ArgumentCaptor<HttpEntity<?>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
			verify(restTemplate).postForObject(eq("github-token-uri"), requestCaptor.capture(), eq(String.class));

			assertEquals("application/json", requestCaptor.getValue().getHeaders().getContentType().toString());
		}

		@Test
		@DisplayName("깃허브 사용자 정보 획득 테스트 - 기본 이메일이 있는 경우")
		void getUserInfo_WithEmail_Success() throws Exception {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.GITHUB;

			// 깃허브 속성 설정
			when(oAuthProperties.getGithub()).thenReturn(githubProperties);
			when(githubProperties.getUserInfoUri()).thenReturn("github-user-info-uri");

			// 깃허브 사용자 정보 응답 준비
			String userInfoResponse = "{"
				+ "\"id\":\"12345\","
				+ "\"email\":\"test@github.com\","
				+ "\"login\":\"깃허브닉네임\","
				+ "\"avatar_url\":\"http://example.com/profile.jpg\""
				+ "}";

			JsonNode userInfoNode = realObjectMapper.readTree(userInfoResponse);

			ResponseEntity<String> responseEntity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);

			when(restTemplate.exchange(
				eq("github-user-info-uri"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(responseEntity);

			when(objectMapper.readTree(userInfoResponse)).thenReturn(userInfoNode);

			// when
			OAuthUserInfoDto result = genericOAuthClient.getUserInfo(accessToken, provider);

			// then
			assertEquals("test@github.com", result.getEmail());
			assertEquals("깃허브닉네임", result.getNickname());
			assertEquals("GITHUB", result.getProvider());
			assertEquals("12345", result.getProviderUserId());
		}

		@Test
		@DisplayName("깃허브 사용자 정보 획득 테스트 - 이메일이 없어 추가 API 호출하는 경우")
		void getUserInfo_WithoutEmail_FetchFromEmailApi_Success() throws Exception {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.GITHUB;

			// 깃허브 속성 설정
			when(oAuthProperties.getGithub()).thenReturn(githubProperties);
			when(githubProperties.getUserInfoUri()).thenReturn("github-user-info-uri");

			// 1. 깃허브 사용자 정보 응답 준비 (이메일 없음)
			String userInfoResponse = "{"
				+ "\"id\":\"12345\","
				+ "\"email\":null,"
				+ "\"login\":\"깃허브닉네임\","
				+ "\"avatar_url\":\"http://example.com/profile.jpg\""
				+ "}";

			// 2. 깃허브 이메일 API 응답 준비
			String emailApiResponse = "["
				+ "{"
				+ "\"email\":\"private@github.com\","
				+ "\"verified\":true,"
				+ "\"primary\":true,"
				+ "\"visibility\":\"private\""
				+ "},"
				+ "{"
				+ "\"email\":\"public@github.com\","
				+ "\"verified\":true,"
				+ "\"primary\":false,"
				+ "\"visibility\":\"public\""
				+ "}"
				+ "]";

			JsonNode userInfoNode = realObjectMapper.readTree(userInfoResponse);
			JsonNode emailApiNode = realObjectMapper.readTree(emailApiResponse);

			ResponseEntity<String> userInfoResponseEntity = new ResponseEntity<>(userInfoResponse, HttpStatus.OK);
			ResponseEntity<String> emailApiResponseEntity = new ResponseEntity<>(emailApiResponse, HttpStatus.OK);

			// 메인 사용자 정보 API 호출 모킹
			when(restTemplate.exchange(
				eq("github-user-info-uri"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(userInfoResponseEntity);

			// 이메일 API 호출 모킹
			when(restTemplate.exchange(
				eq("https://api.github.com/user/emails"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenReturn(emailApiResponseEntity);

			when(objectMapper.readTree(userInfoResponse)).thenReturn(userInfoNode);
			when(objectMapper.readTree(emailApiResponse)).thenReturn(emailApiNode);

			// when
			OAuthUserInfoDto result = genericOAuthClient.getUserInfo(accessToken, provider);

			// then
			assertEquals("private@github.com", result.getEmail()); // primary 이메일이 설정됨
			assertEquals("깃허브닉네임", result.getNickname());
			assertEquals("GITHUB", result.getProvider());
			assertEquals("12345", result.getProviderUserId());

			// 이메일 API가 호출되었는지 검증
			verify(restTemplate).exchange(
				eq("https://api.github.com/user/emails"),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class));
		}
	}

	@Nested
	@DisplayName("오류 처리 테스트")
	class ErrorHandlingTest {

		@Mock
		private OAuthProperties.ProviderProperties naverProperties;

		@Test
		@DisplayName("액세스 토큰 획득 실패 시 예외 발생")
		void getAccessToken_Failure_ThrowsException() throws Exception {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = OAuthProvider.NAVER;

			// 네이버 속성 설정
			when(oAuthProperties.getNaver()).thenReturn(naverProperties);
			when(naverProperties.getTokenUri()).thenReturn("naver-token-uri");

			// 응답에 액세스 토큰이 없는 경우
			ObjectNode tokenResponse = realObjectMapper.createObjectNode();
			tokenResponse.put("error", "invalid_request");

			String tokenResponseString = realObjectMapper.writeValueAsString(tokenResponse);

			when(restTemplate.postForObject(eq("naver-token-uri"), any(HttpEntity.class), eq(String.class)))
				.thenReturn(tokenResponseString);
			when(objectMapper.readTree(tokenResponseString)).thenReturn(tokenResponse);

			// when & then
			ApiException exception = assertThrows(ApiException.class, () ->
				genericOAuthClient.getAccessToken(code, provider));

			assertEquals(ErrorCode.UNAUTHORIZED_OAUTH_FAILED, exception.getErrorCode());
		}

		@Test
		@DisplayName("사용자 정보 획득 실패 시 예외 발생")
		void getUserInfo_Failure_ThrowsException() {
			// given
			String accessToken = "test-access-token";
			OAuthProvider provider = OAuthProvider.NAVER;

			when(oAuthProperties.getNaver()).thenReturn(naverProperties);
			when(naverProperties.getUserInfoUri()).thenReturn("naver-user-info-uri");

			when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(String.class)))
				.thenThrow(new RuntimeException("API 호출 실패"));

			// when & then
			ApiException exception = assertThrows(ApiException.class, () ->
				genericOAuthClient.getUserInfo(accessToken, provider));

			assertEquals(ErrorCode.UNAUTHORIZED_OAUTH_FAILED, exception.getErrorCode());
		}

		@Test
		@DisplayName("지원하지 않는 OAuth 제공자 처리 테스트")
		void unsupportedProvider_ThrowsException() {
			// given
			String code = "test-auth-code";
			OAuthProvider provider = null;

			// when & then
			ApiException exception = assertThrows(ApiException.class, () ->
				genericOAuthClient.getAccessToken(code, provider));

			assertEquals(ErrorCode.UNAUTHORIZED_OAUTH_FAILED, exception.getErrorCode());
		}
	}
}
