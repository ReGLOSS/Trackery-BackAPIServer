package com.trackery.trackerybackapiserver.domain.user.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.entity

 fileName       : CustomUserDetails
 author         : durururuk
 date           : 25. 2. 19.
 description    : 인증된 사용자 정보를 담는 UserDetails 구현체 클래스
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성*/
//TODO 유저 권한 추가 필요
public class CustomUserDetails implements UserDetails {
	private final Long userId;
	private final String userName;
	private final String password;

	public CustomUserDetails(User user) {
		this.userId = user.getUserId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	//인터페이스 메서드 선언이 getUsername()입니다 오타 아닙니다
	@Override
	public String getUsername() {
		return this.userName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return UserDetails.super.isEnabled();
	}
}
