package com.trackery.trackerybackapiserver.domain.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.service

 fileName       : CustomUserDetailService
 author         : durururuk
 date           : 25. 2. 19.
 description    : DB에서 유저 조회 후 인증 객체인 UserDetails로 반환해주는 서비스 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성*/

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

	private final UserMapper userMapper;

	public UserDetails returnUserDetailsByUserId(Long userId) {
		User user = userMapper.findByUserId(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

		if (user.getStatus() == 0) {
			throw new ApiException(ErrorCode.NOT_FOUND);
		}

		return new CustomUserDetails(user);
	}

}
