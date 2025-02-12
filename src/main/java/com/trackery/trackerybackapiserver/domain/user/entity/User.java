package com.trackery.trackerybackapiserver.domain.user.entity;


import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	private Long userId;
	private String email;
	private String userName;
	private String nickname;
	private String password;
	private Timestamp startDate;
	private Integer status;
	private Timestamp lastLogin;
	private String userProfile;

	public static User of(String email, String userName, String nickname, String password, Timestamp startDate, Integer status, Timestamp lastLogin, String userProfile) {
		User user = new User();
		user.email = email;
		user.userName = userName;
		user.nickname = nickname;
		user.password = password;
		user.startDate = startDate;
		user.status = status;
		user.lastLogin = lastLogin;
		user.userProfile = userProfile;
		return user;
	}
}
