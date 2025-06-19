package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.image.service.ImageS3Service;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.jwt.dto.JwtUserInfoDto;
import com.trackery.trackerybackapiserver.domain.jwt.enums.JwtExpirationTime;
import com.trackery.trackerybackapiserver.domain.jwt.service.JwtService;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserProfileDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
import com.trackery.trackerybackapiserver.domain.user.mapper.OAuthMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserRoleMapper;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.service

 fileName       : UserServiceTest
 author         : durururuk
 date           : 25. 2. 14.
 description    : UserService 테스트코드
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 14.        durururuk       최초 생성
 25. 4. 09.		   durururuk	   유저 상세정보 서비스 테스트 코드 작성
 25. 4. 10.		   durururuk	   인증 기반 비밀번호 변경 서비스 단위테스트 코드 작성
 25. 4. 27.		   inari	       사이드탭 추가용 서비스 단위테스트 코드 작성
 */

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Spy
	UserRegisterDto registerDto;
	@Spy
	UserLoginDto loginDto;
	@InjectMocks
	private UserService userService;
	@Mock
	private UserMapper userMapper;
	@Mock
	private UserRoleMapper userRoleMapper;
	@Mock
	private JwtService jwtService;
	@Mock
	private ImageS3Service imageS3Service;

	@Mock
	private OAuthMapper oAuthMapper;

	@Test
	void 회원가입_성공() {
		try (MockedStatic<PasswordUtil> mockedStatic = mockStatic(PasswordUtil.class)) {
			String emailToken = "emailToken";
			String userNameToken = "userNameToken";

			ReflectionTestUtils.setField(registerDto, "nickname", "김커피");
			ReflectionTestUtils.setField(registerDto, "password", "Qwerasdf1234!!asdf");

			DecodedJWT decodedEmailToken = mock(DecodedJWT.class);
			DecodedJWT decodedUserNameToken = mock(DecodedJWT.class);

			when(jwtService.verifyJwt(emailToken)).thenReturn(decodedEmailToken);
			when(jwtService.verifyJwt(userNameToken)).thenReturn(decodedUserNameToken);

			when(decodedEmailToken.getSubject()).thenReturn("a@a.com");
			when(decodedUserNameToken.getSubject()).thenReturn("abcdfg");

			doAnswer(invocation -> {
				User user = invocation.getArgument(0);
				ReflectionTestUtils.setField(user, "userId", 1L);
				return null;
			}).when(userMapper).insertUser(any(User.class));

			doAnswer(invocation -> {
				UserRole userRole = invocation.getArgument(0);
				ReflectionTestUtils.setField(userRole, "userId", 1L);
				ReflectionTestUtils.setField(userRole, "roleId", 1L);
				return null;
			}).when(userRoleMapper).insertUserRole(any(UserRole.class));

			String accessToken = "accessToken";
			String refreshToken = "refreshToken";
			AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);

			when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(
				authTokenDto);

			AuthTokenDto result = userService.registerUser(emailToken, userNameToken, registerDto);

			assertEquals(accessToken, result.accessToken());
			assertEquals(refreshToken, result.refreshToken());

			verify(userMapper, times(1)).insertUser(any(User.class));
			verify(userRoleMapper, times(1)).insertUserRole(any(UserRole.class));

			mockedStatic.verify(() -> PasswordUtil.hashPassword(anyString(), nullable(String.class)), times(1));
			mockedStatic.verify(PasswordUtil::generateSalt, times(1));
		}
	}

	@Test
	void 유저명_중복_확인_성공() {
		when(userMapper.isExistsUserName(anyString())).thenReturn(false);
		when(jwtService.generateTokenWithSubject("abcdefg", JwtExpirationTime.USER_NAME_VERIFICATION_TOKEN)).thenReturn("jwt");

		UserNameAvailabilityResponseDto result = userService.checkUsernameAvailability("abcdefg");

		verify(userMapper, times(1)).isExistsUserName(anyString());

		assertTrue(result.available());
		assertEquals("jwt", result.token());
	}

	@Test
	void 로그인_테스트() {
		ReflectionTestUtils.setField(loginDto, "userName", "abcdfg");
		ReflectionTestUtils.setField(loginDto, "password", "Qwerasdf1234!");

		String salt = "salt";
		String hashedPassword = PasswordUtil.hashPassword("Qwerasdf1234!", salt);

		User user = User.builder()
			.userName("abcdfg")
			.nickname("김커피")
			.password(hashedPassword)
			.salt(salt)
			.status(1)
			.roleId(1L)
			.build();
		ReflectionTestUtils.setField(user, "userId", 1L);

		String accessToken = "access token";
		String refreshToken = "refresh token";
		AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);

		when(userMapper.findByUserName(anyString())).thenReturn(Optional.of(user));

		when(jwtService.generateAccessTokenAndRefreshToken(anyLong(), anyString(), anyLong())).thenReturn(
			authTokenDto);

		AuthTokenDto result = userService.login(loginDto);

		assertEquals(authTokenDto, result);
		verify(userMapper, times(1)).findByUserName(anyString());
	}

	@Nested
	@DisplayName("액세스 토큰 발급 필요 정보 조회 테스트")
	class getUserInfoByIdTest {
		User user = User.builder().userName("abcdefg").roleId(1L).build();
		JwtUserInfoDto expectedDto = new JwtUserInfoDto(1L, "abcdefg", 1L);

		@BeforeEach
		void setUp() {
			ReflectionTestUtils.setField(user, "userId", 1L);
		}

		@Test
		@DisplayName("성공")
		void success() {
			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));

			JwtUserInfoDto result = userService.getUserInfoById(1L);

			assertEquals(expectedDto, result);

			verify(userMapper, times(1)).findByUserId(1L);
		}
	}

	@Nested
	@DisplayName("유저 상세 정보 조회 테스트")
	class getDetailedUserInfoByUserIdTest {
		private User user;
		private OAuth oAuth;

		@BeforeEach
		void setUp() {
			user = User.builder()
				.roleId(1L)
				.userName("abcdefg")
				.nickname("김커피")
				.email("a@a.com")
				.build();

			oAuth = OAuth.builder()
				.userId(1L)
				.providerUserId("155788848")
				.provider("KAKAO")
				.build();

			ReflectionTestUtils.setField(user, "userId", 1L);
			ReflectionTestUtils.setField(oAuth, "oauthId", 1L);
		}

		@Test
		@DisplayName("성공")
		void success() {
			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));
			when(oAuthMapper.findByUserId(1L)).thenReturn(List.of(oAuth));

			DetailedUserInfoDto expect = new DetailedUserInfoDto(1L, 1L,
				"abcdefg", "김커피", "a@a.com", List.of(oAuth));

			DetailedUserInfoDto result = userService.getDetailedUserInfoByUserId(1L);

			assertEquals(expect, result);

			verify(userMapper, times(1)).findByUserId(1L);
			verify(oAuthMapper, times(1)).findByUserId(1L);

		}
	}

	@Nested
	@DisplayName("유저 프로필 정보 조회 테스트")
	class getUserProfileTest {
		private User user;

		@BeforeEach
		void setUp() {
			user = User.builder()
				.userName("abcdefg")
				.nickname("김커피")
				.userProfile("profilePic-object-key")
				.build();

			ReflectionTestUtils.setField(user, "userId", 1L);
		}

		@Test
		@DisplayName("성공 - 프로필 사진이 업로드 돼있던 경우")
		void success_1() {
			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(user));
			when(imageS3Service.generatePreSignedGetUrl("profilePic-object-key")).thenReturn("profilePic-Presigned-url");

			UserProfileDto expect = new UserProfileDto(
				1L,
				"abcdefg",
				"김커피",
				"profilePic-Presigned-url"
			);

			UserProfileDto result = userService.getUserProfile(1L);

			assertEquals(expect.getUserId(), result.getUserId());
			assertEquals(expect.getUserName(), result.getUserName());
			assertEquals(expect.getNickname(), result.getNickname());
			assertEquals(expect.getUserProfilePic(), result.getUserProfilePic());

			verify(userMapper, times(1)).findByUserId(1L);
			verify(imageS3Service, times(1)).generatePreSignedGetUrl("profilePic-object-key");
		}

		@Test
		@DisplayName("성공 - 프로필 사진이 업로드 돼있지 않은 경우")
		void success_2() {
			User userWithoutProfilePic = User.builder()
				.userName("abcdefg")
				.nickname("김커피")
				.userProfile(null)
				.build();

			ReflectionTestUtils.setField(userWithoutProfilePic, "userId", 1L);

			when(userMapper.findByUserId(1L)).thenReturn(Optional.of(userWithoutProfilePic));

			UserProfileDto expect = new UserProfileDto(
				1L,
				"abcdefg",
				"김커피",
				null
			);

			UserProfileDto result = userService.getUserProfile(1L);

			assertEquals(expect.getUserId(), result.getUserId());
			assertEquals(expect.getUserName(), result.getUserName());
			assertEquals(expect.getNickname(), result.getNickname());
			assertEquals(expect.getUserProfilePic(), result.getUserProfilePic());

			verify(userMapper, times(1)).findByUserId(1L);
			verify(imageS3Service, times(0)).generatePreSignedGetUrl(anyString());
		}

		@Test
		@DisplayName("실패 - 사용자 없음")
		void fail_userNotFound() {
			when(userMapper.findByUserId(99L)).thenReturn(Optional.empty());

			ApiException exception = assertThrows(
				ApiException.class,
				() -> userService.getUserProfile(99L)
			);

			assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
			verify(userMapper, times(1)).findByUserId(99L);
		}
	}
}
