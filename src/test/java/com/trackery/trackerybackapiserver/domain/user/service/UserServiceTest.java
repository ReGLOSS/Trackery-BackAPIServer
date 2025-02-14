package com.trackery.trackerybackapiserver.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.service

 fileName       : UserServiceTest
 author         : durururuk
 date           : 25. 2. 14.
 description    :
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

	@Spy
	UserRegisterDto dto;

	@Test
	void 회원가입_성공() {
		try (MockedStatic<PasswordUtil> mockedStatic = mockStatic(PasswordUtil.class)) {
			ReflectionTestUtils.setField(dto, "email", "a@a.com");
			ReflectionTestUtils.setField(dto, "username", "abcdefg");
			ReflectionTestUtils.setField(dto, "nickname", "김커피");
			ReflectionTestUtils.setField(dto, "password", "Qwerasdf1234!!asdf");

			doNothing().when(userMapper).insertUser(any(User.class));
			userService.registerUser(dto);

			verify(userMapper, times(1)).insertUser(any(User.class));

			mockedStatic.verify(() -> PasswordUtil.hashPassword(anyString(), nullable(String.class)), times(1));
			mockedStatic.verify(PasswordUtil::generateSalt, times(1));
		}
	}

	@Test
	void 유저명_중복_확인_성공() {
		when(userMapper.checkUsernameAvailability(anyString())).thenReturn(0);
		userService.checkUsernameAvailability("abcdefg");
		verify(userMapper, times(1)).checkUsernameAvailability(anyString());
		assertTrue(userService.checkUsernameAvailability("abcdefg"));
	}
}