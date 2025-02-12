package com.trackery.trackerybackapiserver.domain.user.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto {

	private String email;
	private String userName;
	private String nickname;
	private String password;
	private String userProfile;
}
