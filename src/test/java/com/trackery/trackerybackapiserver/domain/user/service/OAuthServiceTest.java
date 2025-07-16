package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.config.OAuthProperties;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.client.OAuthClient;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUrlResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;
import com.trackery.trackerybackapiserver.domain.user.mapper.OAuthMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.service
 * fileName       : OAuthServiceTest
 * author         : inari
 * date           : 25. 3. 3.
 * description    : 간편 로그인 관련 비즈니스 로직을 처리하는 서비스 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.		inari		최초 생성
 * 25. 3. 3.		inari		테스트코드 추가
 * 25. 3. 5.		inari		간편로그인시 프로필사진 테스트 제거
 * 25. 3. 5.		durururuk		dev와 병합하면서 생긴 오류 수정
 * 25. 3. 5.		inari		커버리지 리팩터링
 * 25. 3. 27.		inari		상세 주석 추가 및 테스트코드 수정
 * 25. 3. 28.		durururuk		refresh-token/ 리프레시 토큰 레디스 저장 기능 구현
 * 25. 6. 24.		inari		기능 개선 및 테스트코드 수정 및 추가
 * 25. 6. 25.		inari		테스트코드 추가
 * 25. 6. 25.		inari		주석 추가
 * 25. 6. 27.		inari		테스트에 마지막 로그인 시간 추가
 * 25. 6. 27.		inari		테스트코드 작성
 * 25. 6. 28.		inari		코드 복잡도 15이하로 메서드 리팩터링 테스트코드 수정
 * 25. 6. 28.		inari		테스트 코드의 잘못된 eq()수정 및 컨트롤러 문서화 추가
 * 25. 7. 1.		inari		테스트코드 추가
 */
@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

	@InjectMocks
	private OAuthService oAuthService;

	@Mock
	private UserMapper userMapper;

	@Mock
	private OAuthMapper oAuthMapper;

	@Mock
	private UserRoleMapper userRoleMapper;

	@Mock
	private JwtService jwtService;

	@Mock
	private OAuthClient oAuthClient;

	@Mock
	private OAuthProperties oAuthProperties;

	private OAuthLoginDto oAuthLoginDto;
	private OAuthUserInfoDto oAuthUserInfoDto;
	private User user;
	private UserRole userRole;
	private OAuth oAuth;

	@BeforeEach
	void setUp() {
		// 테스트 데이터 설정
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(false)
			.build();

		oAuthUserInfoDto = OAuthUserInfoDto.builder()
			.email("test@example.com")
			.nickname("테스트닉네임")
			.provider("KAKAO")
			.providerUserId("12345")
			.build();

		user = User.builder()
			.email("test@example.com")
			.userName("testuser1234")
			.nickname("테스트닉네임")
			.status(1)
			.build();
		ReflectionTestUtils.setField(user, "userId", 1L);

		userRole = UserRole.builder()
			.userId(1L)
			.roleId(1L)
			.build();

		oAuth = OAuth.builder()
			.userId(1L)
			.provider("KAKAO")
			.providerUserId("12345")
			.build();
	}

	@Test
	void OAuth_로그인_기존_OAuth연동계정_성공() {
		// given
		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.of(oAuth));
		when(userMapper.findByUserId(anyLong())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(new AuthTokenDto("jwt_token", "refresh_token"));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		assertFalse(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByUserId(anyLong());
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtService).generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong());
		verify(userMapper).updateLastLoginByUserId(eq(1L), any(LocalDateTime.class));
	}

	@Test
	void OAuth_로그인_기존_이메일_존재_연동거부_테스트() {
		// given
		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(),any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.of(user));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertNull(result.getJwtToken());
		assertTrue(result.getResponseDto().isExistingEmail());
		assertFalse(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(oAuthMapper, never()).insertOAuth(any(OAuth.class));
	}

	@Test
	void OAuth_로그인_기존_이메일_존재_연동수락_테스트() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(new AuthTokenDto("jwt_token", "refresh_token"));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		assertFalse(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(oAuthMapper).insertOAuth(any(OAuth.class));
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtService).generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong());
		verify(userMapper).updateLastLoginByUserId(eq(1L), any(LocalDateTime.class));
	}

	@Test
	void OAuth_로그인_신규회원가입_성공() {
		// given
		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userMapper.isExistsUserName(anyString())).thenReturn(false);

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			ReflectionTestUtils.setField(user, "userId", 1L);
			return null;
		}).when(userMapper).insertUser(any(User.class));

		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(new AuthTokenDto("jwt_token", "refresh_token"));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		assertTrue(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(),any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(userMapper).insertUser(any(User.class));
		verify(userRoleMapper).insertUserRole(any(UserRole.class));
		verify(oAuthMapper).insertOAuth(any(OAuth.class));
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtService).generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong());
		verify(userMapper).updateLastLoginByUserId(eq(1L), any(LocalDateTime.class));
	}

	@Test
	void OAuth_로그인_linkToken_기반_연동_성공() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.linkUserId(1L)
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(oAuthMapper.findByUserIdAndProvider(anyLong(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByUserId(anyLong())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(new AuthTokenDto("jwt_token", "refresh_token"));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		assertFalse(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(oAuthMapper).findByUserIdAndProvider(anyLong(), anyString());
		verify(userMapper).findByUserId(anyLong());
		verify(oAuthMapper).insertOAuth(any(OAuth.class));
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtService).generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong());
		verify(userMapper).updateLastLoginByUserId(eq(1L), any(LocalDateTime.class));
	}

	@Test
	void generateAuthUrlWithToken_카카오_성공() {
		// given
		OAuthProvider provider = OAuthProvider.KAKAO;
		String linkToken = "test-link-token";
		String baseUrl = "https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=test";

		OAuthProperties.ProviderProperties providerProps = mock(OAuthProperties.ProviderProperties.class);
		when(providerProps.getAuthUri()).thenReturn(baseUrl);
		when(oAuthProperties.getKakao()).thenReturn(providerProps);

		// when
		OAuthUrlResponseDto result = oAuthService.generateAuthUrlWithToken(provider, linkToken);

		// then
		assertNotNull(result);
		assertEquals("KAKAO", result.getProvider());
		assertEquals("link_" + linkToken, result.getState());
		assertTrue(result.getAuthUrl().contains("state=link_" + linkToken));
	}

	@Test
	void generateAuthUrlWithToken_네이버_성공() {
		// given
		OAuthProvider provider = OAuthProvider.NAVER;
		String linkToken = "test-link-token";
		String baseUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=test&redirect_uri=test&state=random_state";

		OAuthProperties.ProviderProperties providerProps = mock(OAuthProperties.ProviderProperties.class);
		when(providerProps.getAuthUri()).thenReturn(baseUrl);
		when(providerProps.getState()).thenReturn("random_state");
		when(oAuthProperties.getNaver()).thenReturn(providerProps);

		// when
		OAuthUrlResponseDto result = oAuthService.generateAuthUrlWithToken(provider, linkToken);

		// then
		assertNotNull(result);
		assertEquals("NAVER", result.getProvider());
		assertEquals("random_state_" + linkToken, result.getState());
		assertTrue(result.getAuthUrl().contains("state=random_state_" + linkToken));
	}

	@Test
	void OAuth_연동모드_다른사용자_OAuth계정_접근차단_테스트() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.linkUserId(2L)  // 현재 사용자 ID = 2
			.build();

		OAuth otherUserOAuth = OAuth.builder()
			.userId(1L)  // 다른 사용자 ID = 1 (OAuth 소유자)
			.provider("KAKAO")
			.providerUserId("12345")
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.of(otherUserOAuth));

		// when & then
		com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException exception = 
			assertThrows(com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException.class, 
				() -> oAuthService.processOAuthLogin(oAuthLoginDto));
		
		assertEquals(com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode.CONFLICT_OAUTH_ALREADY_LINKED, 
			exception.getErrorCode());

		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper, never()).findByUserId(anyLong());
	}

	@Test
	void OAuth_연동모드_본인_OAuth계정_접근허용_테스트() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.linkUserId(1L)  // 현재 사용자 ID = 1
			.build();

		OAuth ownOAuth = OAuth.builder()
			.userId(1L)  // 본인 OAuth 계정 (소유자 ID = 1)
			.provider("KAKAO")
			.providerUserId("12345")
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.of(ownOAuth));
		when(userMapper.findByUserId(anyLong())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(new AuthTokenDto("jwt_token", "refresh_token"));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		assertFalse(result.getResponseDto().isNewUser());
		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		// Mockito에서 정확한 값(1L)을 검증할 때는 eq() matcher를 사용할 필요가 없음
		verify(userMapper).findByUserId(1L);
		verify(userRoleMapper).findByUserId(1L);
		verify(jwtService).generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong());
		verify(userMapper).updateLastLoginByUserId(eq(1L), any(LocalDateTime.class));
	}

	@Test
	void OAuth_linkToken_중복_제공자_연동_실패_테스트() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.linkUserId(1L)
			.build();

		OAuth duplicateOAuth = OAuth.builder()
			.userId(1L)
			.provider("KAKAO")
			.providerUserId("existing_provider_id")
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(oAuthMapper.findByUserIdAndProvider(anyLong(), anyString())).thenReturn(Optional.of(duplicateOAuth));

		// when & then
		com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException exception = 
			assertThrows(com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException.class, 
				() -> oAuthService.processOAuthLogin(oAuthLoginDto));
		
		assertEquals(com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode.DUPLICATE_EMAIL, 
			exception.getErrorCode());

		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(oAuthMapper).findByUserIdAndProvider(anyLong(), anyString());
		verify(userMapper, never()).findByUserId(anyLong());
		verify(oAuthMapper, never()).insertOAuth(any(OAuth.class));
	}

	@Test
	void OAuth_이메일_중복_제공자_연동_실패_테스트() {
		// given
		oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(true)
			.build();

		OAuth duplicateOAuth = OAuth.builder()
			.userId(1L)
			.provider("KAKAO")
			.providerUserId("existing_provider_id")
			.build();

		when(oAuthClient.getAccessToken(anyString(), any(OAuthProvider.class))).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), any(OAuthProvider.class))).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(oAuthMapper.findByUserIdAndProvider(anyLong(), anyString())).thenReturn(Optional.of(duplicateOAuth));

		// when & then
		com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException exception = 
			assertThrows(com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException.class, 
				() -> oAuthService.processOAuthLogin(oAuthLoginDto));
		
		assertEquals(com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode.DUPLICATE_EMAIL, 
			exception.getErrorCode());

		verify(oAuthClient).getAccessToken(anyString(), any(OAuthProvider.class));
		verify(oAuthClient).getUserInfo(anyString(), any(OAuthProvider.class));
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(oAuthMapper).findByUserIdAndProvider(anyLong(), anyString());
		verify(oAuthMapper, never()).insertOAuth(any(OAuth.class));
	}
}
