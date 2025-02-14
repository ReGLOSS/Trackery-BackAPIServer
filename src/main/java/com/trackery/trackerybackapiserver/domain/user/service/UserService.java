package com.trackery.trackerybackapiserver.domain.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

@Service
public class UserService {
	private final UserMapper userMapper;

	public UserService(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	public void registerUser(UserRegisterDto userRegisterDto) {
		User user = User.builder()
			.email(userRegisterDto.getEmail())
			.userName(userRegisterDto.getUserName())
			.nickname(userRegisterDto.getNickname())
			.password(userRegisterDto.getPassword())
			.startDate(Timestamp.valueOf(LocalDateTime.now()))
			.status(1)
			.lastLogin(Timestamp.valueOf(LocalDateTime.now()))
			.userProfile(userRegisterDto.getUserProfile())
			.build();

		userMapper.insertUser(user);
	}

	public boolean checkUsernameAvailability(String username) {
		return userMapper.checkUsernameAvailability(username) == 0;
	}
}
