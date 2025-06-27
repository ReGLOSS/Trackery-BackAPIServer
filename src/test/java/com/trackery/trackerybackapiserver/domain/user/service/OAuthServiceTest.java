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
 * 25. 3. 3.        inari       최초 생성
 * 25. 6. 24.        inari		 기존 유저에 간편 로그인 연동 테스트 추가
 * 25. 6. 25.        inari		 	리프레시 토큰 발급 테스트 추가
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
}
