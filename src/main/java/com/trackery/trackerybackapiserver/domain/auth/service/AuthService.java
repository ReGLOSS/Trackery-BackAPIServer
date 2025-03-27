package com.trackery.trackerybackapiserver.domain.auth.service;

import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.service
 * fileName       : AuthService
 * author         : durururuk
 * date           : 25. 3. 27.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 27.        durururuk      최초 생성
 */
@Service
public class AuthService {
	public AuthUserDto toAuthUserDto(CustomUserDetails customUserDetails) {
		return new AuthUserDto(customUserDetails.getUserId(), customUserDetails.getUsername(),
			customUserDetails.getRoleId());
	}
}
