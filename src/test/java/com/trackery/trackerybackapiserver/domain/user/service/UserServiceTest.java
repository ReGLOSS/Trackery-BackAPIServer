package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.entity.UserRole;
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
 25. 2. 14.        durururuk       최초 생성*/
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@InjectMocks
	private UserService userService;

	@Mock
	private UserMapper userMapper;

	@Mock
	private UserRoleMapper userRoleMapper;

	@Spy
	UserRegisterDto registerDto;

	@Spy
	UserLoginDto loginDto;

	@Mock
	private JwtUtil jwtUtil;

	@Test
	void 회원가입_성공() {
		try (MockedStatic<PasswordUtil> mockedStatic = mockStatic(PasswordUtil.class)) {
			ReflectionTestUtils.setField(registerDto, "email", "a@a.com");
			ReflectionTestUtils.setField(registerDto, "userName", "abcdefg");
			ReflectionTestUtils.setField(registerDto, "nickname", "김커피");
			ReflectionTestUtils.setField(registerDto, "password", "Qwerasdf1234!!asdf");

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

			when(jwtUtil.generateJwt(anyLong(), anyString(), anyLong())).thenReturn("jwt token");

			String result =  userService.registerUser(registerDto);

			assertEquals("jwt token", result);

			verify(userMapper, times(1)).insertUser(any(User.class));
			verify(userRoleMapper, times(1)).insertUserRole(any(UserRole.class));

			mockedStatic.verify(() -> PasswordUtil.hashPassword(anyString(), nullable(String.class)), times(1));
			mockedStatic.verify(PasswordUtil::generateSalt, times(1));
		}
	}

	@Test
	void 유저명_중복_확인_성공() {
		when(userMapper.isExistsUserName(anyString())).thenReturn(false);
		userService.checkUsernameAvailability("abcdefg");
		verify(userMapper, times(1)).isExistsUserName(anyString());
		assertTrue(userService.checkUsernameAvailability("abcdefg"));
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
			.build();
		ReflectionTestUtils.setField(user, "userId", 1L);

		UserRole userRole = UserRole.builder()
			.userId(1L)
			.roleId(1L)
			.build();

		when(userMapper.findByUserName(anyString())).thenReturn(Optional.of(user));
		when(userRoleMapper.findByUserId(anyLong())).thenReturn(Optional.of(userRole));

		when(jwtUtil.generateJwt(anyLong(), anyString(), anyLong())).thenReturn("jwt token");

		String result = userService.login(loginDto);

		assertEquals("jwt token", result);
		verify(userMapper, times(1)).findByUserName(anyString());
		verify(userRoleMapper, times(1)).findByUserId(anyLong());
	}
}