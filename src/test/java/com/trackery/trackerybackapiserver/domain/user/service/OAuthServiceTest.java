package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.user.client.OAuthClient;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
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
	private JwtUtil jwtUtil;

	@Mock
	private OAuthClient oAuthClient;

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
		when(oAuthClient.getAccessToken(anyString(), anyString())).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), anyString())).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.of(oAuth));
		when(userMapper.findByUserId(anyLong())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtUtil.generateAccessToken(anyLong(), anyString(), anyLong())).thenReturn("jwt_token");

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		verify(oAuthClient).getAccessToken(anyString(), anyString());
		verify(oAuthClient).getUserInfo(anyString(), anyString());
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByUserId(anyLong());
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtUtil).generateAccessToken(anyLong(), anyString(), anyLong());
	}

	@Test
	void OAuth_로그인_기존_이메일_존재_연동거부_테스트() {
		// given
		when(oAuthClient.getAccessToken(anyString(), anyString())).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), anyString())).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.of(user));

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertNull(result.getJwtToken());
		assertTrue(result.getResponseDto().isExistingEmail());
		verify(oAuthClient).getAccessToken(anyString(), anyString());
		verify(oAuthClient).getUserInfo(anyString(), anyString());
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

		when(oAuthClient.getAccessToken(anyString(), anyString())).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), anyString())).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtUtil.generateAccessToken(anyLong(), anyString(), anyLong())).thenReturn("jwt_token");

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		verify(oAuthClient).getAccessToken(anyString(), anyString());
		verify(oAuthClient).getUserInfo(anyString(), anyString());
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(oAuthMapper).insertOAuth(any(OAuth.class));
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtUtil).generateAccessToken(anyLong(), anyString(), anyLong());
	}

	@Test
	void OAuth_로그인_신규회원가입_성공() {
		// given
		when(oAuthClient.getAccessToken(anyString(), anyString())).thenReturn("access_token");
		when(oAuthClient.getUserInfo(anyString(), anyString())).thenReturn(oAuthUserInfoDto);
		when(oAuthMapper.findByProviderAndProviderId(anyString(), anyString())).thenReturn(Optional.empty());
		when(userMapper.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userMapper.isExistsUserName(anyString())).thenReturn(false);

		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			ReflectionTestUtils.setField(user, "userId", 1L);
			return null;
		}).when(userMapper).insertUser(any(User.class));

		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));
		when(jwtUtil.generateAccessToken(anyLong(), anyString(), anyLong())).thenReturn("jwt_token");

		// when
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);

		// then
		assertNotNull(result);
		assertEquals("jwt_token", result.getJwtToken());
		assertFalse(result.getResponseDto().isExistingEmail());
		verify(oAuthClient).getAccessToken(anyString(), anyString());
		verify(oAuthClient).getUserInfo(anyString(), anyString());
		verify(oAuthMapper).findByProviderAndProviderId(anyString(), anyString());
		verify(userMapper).findByEmail(anyString());
		verify(userMapper).insertUser(any(User.class));
		verify(userRoleMapper).insertUserRole(any(UserRole.class));
		verify(oAuthMapper).insertOAuth(any(OAuth.class));
		verify(userRoleMapper).findByUserId(anyLong());
		verify(jwtUtil).generateAccessToken(anyLong(), anyString(), anyLong());
	}
}
