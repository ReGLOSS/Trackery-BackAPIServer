package com.trackery.trackerybackapiserver.domain.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.user.dto.UserDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

@Service
public class UserService {
	private final UserMapper userMapper;
	 public UserService(UserMapper userMapper) {
		 this.userMapper = userMapper;
	 }

	public void registerUser(UserDto userDto) {
		User user = User.of(
			userDto.getEmail(),
			userDto.getUserName(),
			userDto.getNickname(),
			userDto.getPassword(),
			Timestamp.valueOf(LocalDateTime.now()),
			1,
			Timestamp.valueOf(LocalDateTime.now()),
			userDto.getUserProfile()
			);

		userMapper.insertUser(user);
	}
}
