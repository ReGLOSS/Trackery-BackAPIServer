package com.trackery.trackerybackapiserver.domain.user.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Getter;

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
	private final Long roleId;

	@Builder
	public CustomUserDetails(Long userId, Long roleId) {
		this.userId = userId;
		this.roleId = roleId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(roleId.toString()));
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return "";
	}

	public Long getUserId() {
		return userId;
	}
}
