package com.trackery.trackerybackapiserver.domain.user.entity;

import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.Builder;
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
	private String salt;
	private Timestamp startDate;
	private Integer status;
	private Timestamp lastLogin;
	private String userProfile;

	@Builder
	public User(String email, String userName, String nickname, String password, String salt, Timestamp startDate,
		Integer status,
		Timestamp lastLogin, String userProfile) {
		this.email = email;
		this.userName = userName;
		this.nickname = nickname;
		this.password = password;
		this.salt = salt;
		this.startDate = startDate;
		this.status = status;
		this.lastLogin = lastLogin;
		this.userProfile = userProfile;
	}
}
