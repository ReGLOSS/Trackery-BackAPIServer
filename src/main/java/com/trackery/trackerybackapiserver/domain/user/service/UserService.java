package com.trackery.trackerybackapiserver.domain.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.util.PasswordUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserMapper userMapper;

	/**
	 * 회원가입 정보를 담아서 db에 인서트하는 메서드
	 *
	 * @param userRegisterDto : 회원 가입 정보를 담은 DTO
	 */
	public void registerUser(UserRegisterDto userRegisterDto) {
		String salt = PasswordUtil.generateSalt();
		String hashedPassword = PasswordUtil.hashPassword(userRegisterDto.getPassword(), salt);

		User user = User.builder()
			.email(userRegisterDto.getEmail())
			.userName(userRegisterDto.getUserName())
			.nickname(userRegisterDto.getNickname())
			.password(hashedPassword)
			.salt(salt)
			.startDate(Timestamp.valueOf(LocalDateTime.now()))
			.status(1)
			.lastLogin(Timestamp.valueOf(LocalDateTime.now()))
			.userProfile(userRegisterDto.getUserProfile())
			.build();

		userMapper.insertUser(user);
	}

	/**
	 * 유저명이 이미 db에 존재하는지 체크하는 메서드
	 *
	 * @param username 조회할 유저명
	 * @return db에 존재하지 않을 경우 true, db에 존재할 경우 false 반환
	 */
	public boolean checkUsernameAvailability(String username) {
		return userMapper.checkUsernameAvailability(username);
	}
}
